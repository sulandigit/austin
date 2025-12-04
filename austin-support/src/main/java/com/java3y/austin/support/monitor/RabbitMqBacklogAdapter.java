package com.java3y.austin.support.monitor;

import com.java3y.austin.common.domain.MqBacklogAlertInfo;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * RabbitMQ积压监控适配器
 *
 * @author austin
 */
@Slf4j
@Component
public class RabbitMqBacklogAdapter implements MqBacklogAdapter {

    @Autowired(required = false)
    private RabbitAdmin rabbitAdmin;

    @Autowired(required = false)
    private ConnectionFactory connectionFactory;

    @Override
    public String getMqType() {
        return "rabbitmq";
    }

    @Override
    public MqBacklogAlertInfo getBacklogInfo(String topic, String consumerGroup) {
        if (rabbitAdmin == null) {
            log.warn("RabbitMQ not configured, skip backlog monitoring");
            return null;
        }

        try {
            // 获取队列属性
            Properties queueProperties = rabbitAdmin.getQueueProperties(topic);
            if (queueProperties == null) {
                log.warn("Queue not found: {}", topic);
                return null;
            }

            // 获取消息数量和消费者数量
            Integer messageCount = (Integer) queueProperties.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
            Integer consumerCount = (Integer) queueProperties.get(RabbitAdmin.QUEUE_CONSUMER_COUNT);

            return MqBacklogAlertInfo.builder()
                    .mqType(getMqType())
                    .topic(topic)
                    .consumerGroup(consumerGroup)
                    .currentBacklog(messageCount != null ? messageCount.longValue() : 0L)
                    .queueDepth(null) // RabbitMQ可配置队列最大长度，这里暂不获取
                    .consumerCount(consumerCount)
                    .alertTimestamp(System.currentTimeMillis())
                    .context("RabbitMQ queue")
                    .build();
        } catch (Exception e) {
            log.error("RabbitMqBacklogAdapter#getBacklogInfo error, topic:{}, consumerGroup:{}", 
                    topic, consumerGroup, e);
            return null;
        }
    }

    @Override
    public boolean checkConnection() {
        if (connectionFactory == null) {
            return false;
        }

        Connection connection = null;
        Channel channel = null;
        try {
            connection = connectionFactory.createConnection();
            channel = connection.createChannel(false);
            return channel.isOpen();
        } catch (Exception e) {
            log.error("RabbitMqBacklogAdapter#checkConnection error", e);
            return false;
        } finally {
            try {
                if (channel != null && channel.isOpen()) {
                    channel.close();
                }
                if (connection != null && connection.isOpen()) {
                    connection.close();
                }
            } catch (Exception e) {
                log.error("RabbitMqBacklogAdapter#checkConnection close error", e);
            }
        }
    }
}
