package com.mall.service.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.enums.PayTypeEnum;
import com.mall.common.enums.PaymentStatusEnum;
import com.mall.common.enums.RefundStatusEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.common.util.SnowflakeIdUtil;
import com.mall.common.util.UserContext;
import com.mall.dao.mapper.OrderMainMapper;
import com.mall.dao.mapper.OrderRefundMapper;
import com.mall.dao.mapper.PaymentRecordMapper;
import com.mall.dao.mapper.PaymentRefundMapper;
import com.mall.model.entity.OrderMain;
import com.mall.model.entity.OrderRefund;
import com.mall.model.entity.PaymentRecord;
import com.mall.model.entity.PaymentRefund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Refund service.
 * Handles refund creation, notify processing, and balance refund.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final PaymentRefundMapper paymentRefundMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final OrderMainMapper orderMainMapper;
    private final OrderRefundMapper orderRefundMapper;
    private final PaymentFactory paymentFactory;

    private static final String REFUND_NO_PREFIX = "REF";

    /**
     * Create a refund record and invoke the payment strategy refund.
     *
     * @param orderItemId order item ID
     * @param amount      refund amount
     * @param reason      refund reason
     * @return refund number
     */
    @Transactional(rollbackFor = Exception.class)
    public String createRefund(Long orderItemId, BigDecimal amount, String reason) {
        Long memberId = UserContext.getUserId();
        if (memberId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // Find the order refund record
        LambdaQueryWrapper<OrderRefund> refundQuery = new LambdaQueryWrapper<>();
        refundQuery.eq(OrderRefund::getOrderItemId, orderItemId)
                .eq(OrderRefund::getMemberId, memberId)
                .orderByDesc(OrderRefund::getCreateTime)
                .last("LIMIT 1");
        OrderRefund orderRefund = orderRefundMapper.selectOne(refundQuery);
        if (orderRefund == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "退款申请不存在");
        }

        // Find the payment record
        LambdaQueryWrapper<PaymentRecord> paymentQuery = new LambdaQueryWrapper<>();
        paymentQuery.eq(PaymentRecord::getOrderId, orderRefund.getOrderId())
                .eq(PaymentRecord::getStatus, PaymentStatusEnum.PAID.getCode())
                .last("LIMIT 1");
        PaymentRecord paymentRecord = paymentRecordMapper.selectOne(paymentQuery);
        if (paymentRecord == null) {
            throw new BusinessException(ResultCode.PAYMENT_FAIL.getCode(), "未找到已支付的支付记录");
        }

        // Validate refund amount does not exceed payment amount (S-06)
        if (amount != null && amount.compareTo(paymentRecord.getPayAmount()) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "退款金额不能超过支付金额");
        }

        // Create refund record
        PaymentRefund refundRecord = new PaymentRefund();
        refundRecord.setRefundNo(generateRefundNo());
        refundRecord.setPaymentId(paymentRecord.getId());
        refundRecord.setPaymentNo(paymentRecord.getPaymentNo());
        refundRecord.setOrderId(orderRefund.getOrderId());
        refundRecord.setOrderNo(paymentRecord.getOrderNo());
        refundRecord.setMemberId(memberId);
        refundRecord.setRefundAmount(amount);
        refundRecord.setRefundReason(reason);
        refundRecord.setStatus(RefundStatusEnum.REFUNDING.getCode());
        refundRecord.setTenantId(paymentRecord.getTenantId());
        paymentRefundMapper.insert(refundRecord);

        // Process refund via strategy
        PayStrategy strategy = paymentFactory.getStrategy(paymentRecord.getPayType());
        PayStrategy.RefundResult refundResult = strategy.refund(refundRecord);

        if (refundResult.isSuccess()) {
            // Mark refund success
            markRefundSuccess(refundRecord.getRefundNo(), refundResult.getTradeNo());
        } else {
            // Mark refund failed
            markRefundFailed(refundRecord.getRefundNo(), refundResult.getMessage());
        }

        log.info("[Refund] Refund created: refundNo={}, amount={}, result={}",
                refundRecord.getRefundNo(), amount, refundResult.isSuccess());
        return refundRecord.getRefundNo();
    }

    /**
     * Handle refund callback notification.
     *
     * @param channel payment channel name
     * @param params  callback parameters
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleRefundNotify(String channel, Map<String, String> params) {
        Integer payType = resolvePayType(channel);
        PayStrategy strategy = paymentFactory.getStrategy(payType);

        if (!strategy.verifyNotify(params)) {
            log.warn("[Refund] Notify verification failed: channel={}", channel);
            throw new BusinessException("退款回调验签失败");
        }

        String refundNo = params.getOrDefault("out_refund_no", "");
        PaymentRefund refundRecord = getByRefundNo(refundNo);
        if (refundRecord == null) {
            log.warn("[Refund] Refund record not found: refundNo={}", refundNo);
            return;
        }

        // Idempotent check
        if (refundRecord.getStatus() != RefundStatusEnum.REFUNDING.getCode()) {
            log.info("[Refund] Refund already processed: refundNo={}, status={}", refundNo, refundRecord.getStatus());
            return;
        }

        String tradeNo = params.getOrDefault("refund_id", params.getOrDefault("trade_no", ""));
        boolean success = "SUCCESS".equalsIgnoreCase(params.getOrDefault("refund_status", "FAIL"));

        if (success) {
            markRefundSuccess(refundNo, tradeNo);
        } else {
            markRefundFailed(refundNo, params.getOrDefault("refund_msg", "退款失败"));
        }

        log.info("[Refund] Refund notify processed: refundNo={}, success={}", refundNo, success);
    }

    /**
     * Refund to member balance directly.
     *
     * @param memberId member ID
     * @param amount   refund amount
     */
    @Transactional(rollbackFor = Exception.class)
    public void refundToBalance(Long memberId, BigDecimal amount) {
        int rows = paymentRecordMapper.addBalance(memberId, amount);
        if (rows == 0) {
            throw new BusinessException(ResultCode.PAYMENT_FAIL.getCode(), "余额退款失败: 会员不存在");
        }
        log.info("[Refund] Balance refund success: memberId={}, amount={}", memberId, amount);
    }

    // ===== Private helper methods =====

    private PaymentRefund getByRefundNo(String refundNo) {
        LambdaQueryWrapper<PaymentRefund> query = new LambdaQueryWrapper<>();
        query.eq(PaymentRefund::getRefundNo, refundNo);
        return paymentRefundMapper.selectOne(query);
    }

    private void markRefundSuccess(String refundNo, String tradeNo) {
        LambdaUpdateWrapper<PaymentRefund> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PaymentRefund::getRefundNo, refundNo)
                .eq(PaymentRefund::getStatus, RefundStatusEnum.REFUNDING.getCode())
                .set(PaymentRefund::getStatus, RefundStatusEnum.SUCCESS.getCode())
                .set(PaymentRefund::getTradeNo, tradeNo)
                .set(PaymentRefund::getRefundTime, LocalDateTime.now());
        paymentRefundMapper.update(null, updateWrapper);

        // Update payment record status
        PaymentRefund refundRecord = getByRefundNo(refundNo);
        if (refundRecord != null) {
            LambdaUpdateWrapper<PaymentRecord> paymentUpdate = new LambdaUpdateWrapper<>();
            paymentUpdate.eq(PaymentRecord::getPaymentNo, refundRecord.getPaymentNo())
                    .set(PaymentRecord::getStatus, PaymentStatusEnum.REFUNDED.getCode());
            paymentRecordMapper.update(null, paymentUpdate);
        }
    }

    private void markRefundFailed(String refundNo, String message) {
        LambdaUpdateWrapper<PaymentRefund> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PaymentRefund::getRefundNo, refundNo)
                .eq(PaymentRefund::getStatus, RefundStatusEnum.REFUNDING.getCode())
                .set(PaymentRefund::getStatus, RefundStatusEnum.FAIL.getCode())
                .set(PaymentRefund::getCallbackData, message);
        paymentRefundMapper.update(null, updateWrapper);
    }

    private String generateRefundNo() {
        return REFUND_NO_PREFIX + SnowflakeIdUtil.getInstance().nextIdStr();
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
