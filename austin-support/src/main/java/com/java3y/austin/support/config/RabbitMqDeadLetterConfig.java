package com.java3y.austin.support.config;

import com.java3y.austin.support.constans.MessageQueuePipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 死信队列配置
 *
 * @author austin
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "austin.mq.pipeline", havingValue = MessageQueuePipeline.RABBIT_MQ)
public class RabbitMqDeadLetterConfig {

    @Value("${austin.rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${spring.rabbitmq.queues.send}")
    private String sendQueueName;

    @Value("${spring.rabbitmq.queues.recall}")
    private String recallQueueName;

    @Value("${austin.rabbitmq.routing.send}")
    private String sendRoutingKey;

    @Value("${austin.rabbitmq.routing.recall}")
    private String recallRoutingKey;

    @Value("${austin.rabbitmq.dead-letter.exchange.name:austin.dlx}")
    private String deadLetterExchange;

    @Value("${austin.rabbitmq.dead-letter.queue.send:austin.dlq.send}")
    private String deadLetterSendQueue;

    @Value("${austin.rabbitmq.dead-letter.queue.recall:austin.dlq.recall}")
    private String deadLetterRecallQueue;

    @Value("${austin.rabbitmq.dead-letter.routing.send:austin.dlx.send}")
    private String deadLetterSendRoutingKey;

    @Value("${austin.rabbitmq.dead-letter.routing.recall:austin.dlx.recall}")
    private String deadLetterRecallRoutingKey;

    @Value("${austin.rabbitmq.queue.ttl:3600000}")
    private Integer messageTtl;

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchange, true, false);
    }

    /**
     * 死信队列 - send
     */
    @Bean
    public Queue deadLetterSendQueue() {
        return QueueBuilder.durable(deadLetterSendQueue).build();
    }

    /**
     * 死信队列 - recall
     */
    @Bean
    public Queue deadLetterRecallQueue() {
        return QueueBuilder.durable(deadLetterRecallQueue).build();
    }

    /**
     * 绑定死信队列到死信交换机 - send
     */
    @Bean
    public Binding deadLetterSendBinding() {
        return BindingBuilder.bind(deadLetterSendQueue())
                .to(deadLetterExchange())
                .with(deadLetterSendRoutingKey);
    }

    /**
     * 绑定死信队列到死信交换机 - recall
     */
    @Bean
    public Binding deadLetterRecallBinding() {
        return BindingBuilder.bind(deadLetterRecallQueue())
                .to(deadLetterExchange())
                .with(deadLetterRecallRoutingKey);
    }

    /**
     * 业务队列 - send (带死信配置)
     */
    @Bean
    public Queue sendQueue() {
        Map<String, Object> args = new HashMap<>(4);
        // 配置死信交换机
        args.put("x-dead-letter-exchange", deadLetterExchange);
        // 配置死信路由键
        args.put("x-dead-letter-routing-key", deadLetterSendRoutingKey);
        // 配置消息TTL（可选）
        // args.put("x-message-ttl", messageTtl);
        return QueueBuilder.durable(sendQueueName).withArguments(args).build();
    }

    /**
     * 业务队列 - recall (带死信配置)
     */
    @Bean
    public Queue recallQueue() {
        Map<String, Object> args = new HashMap<>(4);
        args.put("x-dead-letter-exchange", deadLetterExchange);
        args.put("x-dead-letter-routing-key", deadLetterRecallRoutingKey);
        // args.put("x-message-ttl", messageTtl);
        return QueueBuilder.durable(recallQueueName).withArguments(args).build();
    }

    /**
     * 业务交换机
     */
    @Bean
    public TopicExchange businessExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    /**
     * 绑定业务队列到业务交换机 - send
     */
    @Bean
    public Binding sendBinding() {
        return BindingBuilder.bind(sendQueue())
                .to(businessExchange())
                .with(sendRoutingKey);
    }

    /**
     * 绑定业务队列到业务交换机 - recall
     */
    @Bean
    public Binding recallBinding() {
        return BindingBuilder.bind(recallQueue())
                .to(businessExchange())
                .with(recallRoutingKey);
    }
}
