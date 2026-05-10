package com.mall.service.mq.consumer;

import com.mall.service.config.RabbitMQConfig;
import com.mall.service.promotion.SeckillActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderConsumer {

    private final SeckillActivityService seckillActivityService;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_ORDER_QUEUE)
    public void handleSeckillOrder(String message) {
        log.info("[MQ] Processing seckill order: {}", message);
        try {
            String[] parts = message.split(":");
            Long memberId = Long.parseLong(parts[0]);
            Long activityId = Long.parseLong(parts[1]);
            Long skuId = Long.parseLong(parts[2]);
            seckillActivityService.createSeckillOrder(memberId, activityId, skuId);
        } catch (Exception e) {
            log.error("[MQ] Failed to process seckill order: {}", message, e);
        }
    }
}
