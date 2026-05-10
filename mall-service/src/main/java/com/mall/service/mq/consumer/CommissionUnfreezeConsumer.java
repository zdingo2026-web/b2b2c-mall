package com.mall.service.mq.consumer;

import com.mall.service.config.RabbitMQConfig;
import com.mall.service.distribution.CommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommissionUnfreezeConsumer {

    private final CommissionService commissionService;

    @RabbitListener(queues = RabbitMQConfig.COMMISSION_UNFREEZE_QUEUE)
    public void handleCommissionUnfreeze(String commissionId) {
        log.info("[MQ] Processing commission unfreeze: commissionId={}", commissionId);
        try {
            commissionService.unfreezeCommission(null, Long.parseLong(commissionId));
        } catch (Exception e) {
            log.error("[MQ] Failed to unfreeze commission: {}", commissionId, e);
        }
    }
}
