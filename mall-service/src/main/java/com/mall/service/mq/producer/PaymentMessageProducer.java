package com.mall.service.mq.producer;

import com.mall.service.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Payment message producer.
 * Sends payment success messages to RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Send a payment success message.
     *
     * @param orderNo order number
     */
    public void sendPaySuccessMessage(String orderNo) {
        log.info("[MQ] Sending pay success message: orderNo={}", orderNo);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAY_SUCCESS_EXCHANGE,
                RabbitMQConfig.PAY_SUCCESS_ROUTING_KEY,
                orderNo
        );
    }
}
