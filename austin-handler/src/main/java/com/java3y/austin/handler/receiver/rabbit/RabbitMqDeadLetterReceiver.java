package com.java3y.austin.handler.receiver.rabbit;

import com.alibaba.fastjson.JSON;
import com.java3y.austin.common.domain.RecallTaskInfo;
import com.java3y.austin.common.domain.TaskInfo;
import com.java3y.austin.handler.receiver.service.DeadLetterService;
import com.java3y.austin.support.constans.MessageQueuePipeline;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * RabbitMQ 死信队列消费者
 * 处理业务队列消费失败后进入死信队列的消息
 *
 * @author austin
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "austin.mq.pipeline", havingValue = MessageQueuePipeline.RABBIT_MQ)
public class RabbitMqDeadLetterReceiver {

    @Autowired
    private DeadLetterService deadLetterService;

    /**
     * 消费死信队列 - send
     *
     * @param message 死信消息
     */
    @RabbitListener(queues = "${austin.rabbitmq.dead-letter.queue.send:austin.dlq.send}")
    public void handleDeadLetterSend(Message message) {
        try {
            byte[] body = message.getBody();
            String messageContent = new String(body, StandardCharsets.UTF_8);
            
            if (StringUtils.isBlank(messageContent)) {
                log.warn("死信队列收到空消息");
                return;
            }

            log.error("收到发送消息死信，消息内容：{}，消息属性：{}", messageContent, message.getMessageProperties());

            // 解析消息
            List<TaskInfo> taskInfoLists = JSON.parseArray(messageContent, TaskInfo.class);
            
            // 处理死信消息
            deadLetterService.handleDeadLetterSend(taskInfoLists, message);
            
        } catch (Exception e) {
            log.error("处理发送消息死信失败", e);
            // 这里可以选择将消息持久化到数据库或者发送告警
            deadLetterService.handleDeadLetterException(message, e);
        }
    }

    /**
     * 消费死信队列 - recall
     *
     * @param message 死信消息
     */
    @RabbitListener(queues = "${austin.rabbitmq.dead-letter.queue.recall:austin.dlq.recall}")
    public void handleDeadLetterRecall(Message message) {
        try {
            byte[] body = message.getBody();
            String messageContent = new String(body, StandardCharsets.UTF_8);
            
            if (StringUtils.isBlank(messageContent)) {
                log.warn("死信队列收到空消息");
                return;
            }

            log.error("收到撤回消息死信，消息内容：{}，消息属性：{}", messageContent, message.getMessageProperties());

            // 解析消息
            RecallTaskInfo recallTaskInfo = JSON.parseObject(messageContent, RecallTaskInfo.class);
            
            // 处理死信消息
            deadLetterService.handleDeadLetterRecall(recallTaskInfo, message);
            
        } catch (Exception e) {
            log.error("处理撤回消息死信失败", e);
            deadLetterService.handleDeadLetterException(message, e);
        }
    }
}
