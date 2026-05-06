package com.mall.service.mq.producer;

import com.mall.service.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Order message producer.
 * Sends order timeout delay messages to RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Send an order timeout delay message.
     * The message will be delivered to the timeout queue after 30 minutes.
     *
     * @param orderNo order number
     */
    public void sendOrderTimeoutMessage(String orderNo) {
        log.info("[MQ] Sending order timeout message: orderNo={}", orderNo);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_DELAY_QUEUE,
                "",
                orderNo
        );
    }
}
