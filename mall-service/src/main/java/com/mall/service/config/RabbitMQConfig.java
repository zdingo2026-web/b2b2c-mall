package com.mall.service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration.
 * Defines exchanges, queues, and bindings for:
 * - Order timeout (DLX + TTL)
 * - Payment success
 * - SMS notifications
 */
@Configuration
public class RabbitMQConfig {

    // ===== Order Timeout (DLX + TTL) =====

    /** Order timeout exchange (DLX) */
    public static final String ORDER_TIMEOUT_EXCHANGE = "mall.order.timeout.exchange";

    /** Order timeout queue (destination after TTL expires) */
    public static final String ORDER_TIMEOUT_QUEUE = "mall.order.timeout.queue";

    /** Order delay queue (messages wait here with TTL, then route to DLX) */
    public static final String ORDER_DELAY_QUEUE = "mall.order.delay.queue";

    /** Order routing key */
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "mall.order.timeout";

    /** Order TTL in milliseconds (30 minutes) */
    public static final long ORDER_TIMEOUT_TTL = 30 * 60 * 1000L;

    // ===== Payment Success =====

    public static final String PAY_SUCCESS_EXCHANGE = "mall.pay.success.exchange";
    public static final String PAY_SUCCESS_QUEUE = "mall.pay.success.queue";
    public static final String PAY_SUCCESS_ROUTING_KEY = "mall.pay.success";

    // ===== SMS =====

    public static final String SMS_EXCHANGE = "mall.sms.exchange";
    public static final String SMS_QUEUE = "mall.sms.queue";
    public static final String SMS_ROUTING_KEY = "mall.sms.send";

    // ===== JSON Message Converter =====

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setPrefetchCount(10);
        return factory;
    }

    // ===== Order Timeout Queues =====

    @Bean
    public DirectExchange orderTimeoutExchange() {
        return new DirectExchange(ORDER_TIMEOUT_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_QUEUE).build();
    }

    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(ORDER_DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_TIMEOUT_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_TIMEOUT_ROUTING_KEY)
                .withArgument("x-message-ttl", ORDER_TIMEOUT_TTL)
                .build();
    }

    @Bean
    public Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue())
                .to(orderTimeoutExchange())
                .with(ORDER_TIMEOUT_ROUTING_KEY);
    }

    // ===== Payment Success Queues =====

    @Bean
    public DirectExchange paySuccessExchange() {
        return new DirectExchange(PAY_SUCCESS_EXCHANGE, true, false);
    }

    @Bean
    public Queue paySuccessQueue() {
        return QueueBuilder.durable(PAY_SUCCESS_QUEUE).build();
    }

    @Bean
    public Binding paySuccessBinding() {
        return BindingBuilder.bind(paySuccessQueue())
                .to(paySuccessExchange())
                .with(PAY_SUCCESS_ROUTING_KEY);
    }

    // ===== SMS Queues =====

    @Bean
    public DirectExchange smsExchange() {
        return new DirectExchange(SMS_EXCHANGE, true, false);
    }

    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable(SMS_QUEUE).build();
    }

    @Bean
    public Binding smsBinding() {
        return BindingBuilder.bind(smsQueue())
                .to(smsExchange())
                .with(SMS_ROUTING_KEY);
    }
}
