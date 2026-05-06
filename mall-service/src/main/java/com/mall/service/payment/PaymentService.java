package com.mall.service.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.enums.OrderStatusEnum;
import com.mall.common.enums.PayTypeEnum;
import com.mall.common.enums.PaymentStatusEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.constant.RedisKeyConstant;
import com.mall.common.response.ResultCode;
import com.mall.common.util.SnowflakeIdUtil;
import com.mall.common.util.UserContext;
import com.mall.dao.mapper.OrderMainMapper;
import com.mall.dao.mapper.PaymentRecordMapper;
import com.mall.model.entity.OrderMain;
import com.mall.model.entity.PaymentRecord;
import com.mall.service.mq.producer.PaymentMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Payment service.
 * Handles payment creation, notify processing, status query, and close.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRecordMapper paymentRecordMapper;
    private final OrderMainMapper orderMainMapper;
    private final PaymentFactory paymentFactory;
    private final PaymentMessageProducer paymentMessageProducer;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String PAYMENT_NO_PREFIX = "PAY";
    private static final int PAY_EXPIRE_MINUTES = 30;

    /**
     * Create a payment record and invoke the payment strategy.
     *
     * @param orderId order ID
     * @param payType payment type code
     * @return payment parameters for the client
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPayment(Long orderId, Integer payType) {
        Long memberId = UserContext.getUserId();
        if (memberId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // Acquire distributed lock per orderId to prevent concurrent payments
        String lockKey = RedisKeyConstant.PAYMENT_ORDER_LOCK + orderId;
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            throw new BusinessException("支付处理中，请勿重复操作");
        }

        try {
        // Validate order
        OrderMain order = orderMainMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getOrderStatus() != OrderStatusEnum.PENDING_PAYMENT.getCode()) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_PAY);
        }

        // Check for existing pending payment
        PaymentRecord existingRecord = getPendingPayment(orderId);
        if (existingRecord != null) {
            // Close the existing pending payment
            closePayment(existingRecord.getPaymentNo());
        }

        // Create payment record
        PaymentRecord record = new PaymentRecord();
        record.setPaymentNo(generatePaymentNo());
        record.setOrderId(orderId);
        record.setOrderNo(order.getOrderNo());
        record.setMemberId(memberId);
        record.setPayType(payType);
        record.setPayAmount(order.getPayAmount());
        record.setStatus(PaymentStatusEnum.PENDING.getCode());
        record.setExpireTime(LocalDateTime.now().plusMinutes(PAY_EXPIRE_MINUTES));
        record.setTenantId(order.getTenantId());
        paymentRecordMapper.insert(record);

        // Invoke payment strategy
        PayStrategy strategy = paymentFactory.getStrategy(payType);
        Map<String, Object> payParams = strategy.createPay(record);

        // For balance payment, it's synchronous: mark as paid immediately
        if (PayTypeEnum.BALANCE.getCode() == payType) {
            markPaymentSuccess(record.getPaymentNo(), "balance_direct", order);
        }

        // Add paymentNo to result
        payParams.put("paymentNo", record.getPaymentNo());
        return payParams;
        } finally {
            // Release distributed lock
            stringRedisTemplate.delete(lockKey);
        }
    }

    /**
     * Process payment callback notification.
     * Idempotent: checks payment status before updating.
     *
     * @param channel payment channel name (wechat/alipay)
     * @param params  callback parameters
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleNotify(String channel, Map<String, String> params) {
        Integer payType = resolvePayType(channel);
        PayStrategy strategy = paymentFactory.getStrategy(payType);

        // Verify signature
        if (!strategy.verifyNotify(params)) {
            log.warn("[Payment] Notify verification failed: channel={}", channel);
            throw new BusinessException("回调验签失败");
        }

        PayStrategy.PayNotifyResult notifyResult = strategy.handleNotify(params);
        if (!notifyResult.isSuccess()) {
            log.warn("[Payment] Notify result indicates failure: paymentNo={}", notifyResult.getPaymentNo());
            return;
        }

        // Idempotent check: only process if status is PENDING
        PaymentRecord record = getByPaymentNo(notifyResult.getPaymentNo());
        if (record == null) {
            log.warn("[Payment] Payment record not found: paymentNo={}", notifyResult.getPaymentNo());
            return;
        }
        if (record.getStatus() != PaymentStatusEnum.PENDING.getCode()) {
            log.info("[Payment] Payment already processed: paymentNo={}, status={}", record.getPaymentNo(), record.getStatus());
            return;
        }

        // Find the order
        OrderMain order = orderMainMapper.selectById(record.getOrderId());
        if (order == null) {
            log.warn("[Payment] Order not found: orderId={}", record.getOrderId());
            return;
        }

        // Mark payment success
        markPaymentSuccess(record.getPaymentNo(), notifyResult.getTradeNo(), order);

        // Update payment record with trade info (only if still PENDING to prevent race condition)
        LambdaUpdateWrapper<PaymentRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PaymentRecord::getPaymentNo, record.getPaymentNo())
                .eq(PaymentRecord::getStatus, PaymentStatusEnum.PENDING.getCode())
                .set(PaymentRecord::getCallbackData, params.toString());
        paymentRecordMapper.update(null, updateWrapper);

        log.info("[Payment] Notify processed successfully: paymentNo={}", record.getPaymentNo());
    }

    /**
     * Query payment status by payment number.
     *
     * @param paymentNo payment number
     * @return payment status info
     */
    public Map<String, Object> queryPaymentStatus(String paymentNo) {
        PaymentRecord record = getByPaymentNo(paymentNo);
        if (record == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("paymentNo", record.getPaymentNo());
        result.put("status", record.getStatus());
        result.put("payAmount", record.getPayAmount());
        result.put("payType", record.getPayType());
        result.put("payTime", record.getPayTime());
        return result;
    }

    /**
     * Close a pending payment.
     *
     * @param paymentNo payment number
     */
    @Transactional(rollbackFor = Exception.class)
    public void closePayment(String paymentNo) {
        PaymentRecord record = getByPaymentNo(paymentNo);
        if (record == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (record.getStatus() != PaymentStatusEnum.PENDING.getCode()) {
            throw new BusinessException("支付单状态不允许关闭");
        }

        LambdaUpdateWrapper<PaymentRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PaymentRecord::getPaymentNo, paymentNo)
                .eq(PaymentRecord::getStatus, PaymentStatusEnum.PENDING.getCode())
                .set(PaymentRecord::getStatus, PaymentStatusEnum.CLOSED.getCode());
        paymentRecordMapper.update(null, updateWrapper);

        log.info("[Payment] Payment closed: paymentNo={}", paymentNo);
    }

    // ===== Private helper methods =====

    private PaymentRecord getPendingPayment(Long orderId) {
        LambdaQueryWrapper<PaymentRecord> query = new LambdaQueryWrapper<>();
        query.eq(PaymentRecord::getOrderId, orderId)
                .eq(PaymentRecord::getStatus, PaymentStatusEnum.PENDING.getCode())
                .last("LIMIT 1");
        return paymentRecordMapper.selectOne(query);
    }

    private PaymentRecord getByPaymentNo(String paymentNo) {
        LambdaQueryWrapper<PaymentRecord> query = new LambdaQueryWrapper<>();
        query.eq(PaymentRecord::getPaymentNo, paymentNo);
        return paymentRecordMapper.selectOne(query);
    }

    private void markPaymentSuccess(String paymentNo, String tradeNo, OrderMain order) {
        // Update payment record
        LambdaUpdateWrapper<PaymentRecord> paymentUpdate = new LambdaUpdateWrapper<>();
        paymentUpdate.eq(PaymentRecord::getPaymentNo, paymentNo)
                .eq(PaymentRecord::getStatus, PaymentStatusEnum.PENDING.getCode())
                .set(PaymentRecord::getStatus, PaymentStatusEnum.PAID.getCode())
                .set(PaymentRecord::getTradeNo, tradeNo)
                .set(PaymentRecord::getPayTime, LocalDateTime.now());
        int paymentRows = paymentRecordMapper.update(null, paymentUpdate);
        if (paymentRows == 0) {
            throw new BusinessException("支付单状态已变更，请勿重复支付");
        }

        // Update order status
        LambdaUpdateWrapper<OrderMain> orderUpdate = new LambdaUpdateWrapper<>();
        orderUpdate.eq(OrderMain::getId, order.getId())
                .eq(OrderMain::getOrderStatus, OrderStatusEnum.PENDING_PAYMENT.getCode())
                .set(OrderMain::getOrderStatus, OrderStatusEnum.PENDING_SHIPMENT.getCode())
                .set(OrderMain::getPayStatus, PaymentStatusEnum.PAID.getCode())
                .set(OrderMain::getPayType, order.getPayType())
                .set(OrderMain::getPayTime, LocalDateTime.now());
        int orderRows = orderMainMapper.update(null, orderUpdate);
        if (orderRows == 0) {
            throw new BusinessException("订单状态已变更，请勿重复支付");
        }

        // Send payment success message
        try {
            paymentMessageProducer.sendPaySuccessMessage(order.getOrderNo());
        } catch (Exception e) {
            log.error("[Payment] Failed to send pay success message: orderNo={}", order.getOrderNo(), e);
        }

        log.info("[Payment] Payment success: paymentNo={}, orderNo={}", paymentNo, order.getOrderNo());
    }

    private String generatePaymentNo() {
        return PAYMENT_NO_PREFIX + SnowflakeIdUtil.getInstance().nextIdStr();
    }

    private Integer resolvePayType(String channel) {
        switch (channel.toLowerCase()) {
            case "wechat":
                return PayTypeEnum.WECHAT.getCode();
            case "alipay":
                return PayTypeEnum.ALIPAY.getCode();
            default:
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不支持的支付渠道: " + channel);
        }
    }
}
