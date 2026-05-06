package com.mall.service.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.util.TenantContext;
import com.mall.common.util.UserContext;
import com.mall.dao.mapper.PaymentRecordMapper;
import com.mall.model.entity.PaymentRecord;
import com.mall.service.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Payment success consumer.
 * Processes payment success events: sends SMS notifications.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaySuccessConsumer {

    private final PaymentRecordMapper paymentRecordMapper;

    /**
     * Consume payment success messages.
     *
     * @param orderNo order number
     */
    @RabbitListener(queues = RabbitMQConfig.PAY_SUCCESS_QUEUE)
    public void handlePaySuccess(String orderNo) {
        log.info("[MQ] Processing payment success: orderNo={}", orderNo);

        try {
            // Load payment record to establish tenant context for proper isolation
            PaymentRecord paymentRecord = paymentRecordMapper.selectOne(
                    new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getOrderNo, orderNo));
            if (paymentRecord != null) {
                TenantContext.setTenantId(paymentRecord.getTenantId());
            }

            // TODO: Send SMS notification to member
            // smsService.sendPaySuccessSms(order.getPhone(), orderNo);
            log.info("[MQ] Payment success notification sent (mock): orderNo={}", orderNo);
        } finally {
            // Clear context to prevent ThreadLocal leaks on reused MQ consumer threads
            UserContext.clear();
            TenantContext.clear();
        }
    }
}
