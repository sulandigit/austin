package com.java3y.austin.support.mq.springeventbus;

import cn.hutool.core.text.CharSequenceUtil;
import com.java3y.austin.support.constans.MessageQueuePipeline;
import com.java3y.austin.support.mq.SendMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Spring Event Bus消息发送服务实现类
 * <p>
 * 基于Spring事件机制实现的消息队列服务，适用于单体应用内的异步消息传递
 * 通过发布ApplicationEvent来实现消息的发送和订阅
 * </p>
 *
 * @author tony
 * @date 2023/2/6 11:11
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "austin.mq.pipeline", havingValue = MessageQueuePipeline.SPRING_EVENT_BUS)
public class SpringEventBusSendMqServiceImpl implements SendMqService {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 构造器注入ApplicationContext
     * 使用ApplicationEventPublisher接口，更符合单一职责原则
     *
     * @param applicationContext Spring应用上下文
     */
    public SpringEventBusSendMqServiceImpl(ApplicationContext applicationContext) {
        this.eventPublisher = applicationContext;
    }

    /**
     * 发送消息到Spring Event Bus
     *
     * @param topic     消息主题
     * @param jsonValue 消息内容（JSON格式）
     * @param tagId     消息标签ID
     */
    @Override
    public void send(String topic, String jsonValue, String tagId) {
        // 参数校验
        if (CharSequenceUtil.isBlank(topic)) {
            log.error("[SpringEventBusSendMqServiceImpl#send] topic不能为空");
            throw new IllegalArgumentException("topic不能为空");
        }
        if (CharSequenceUtil.isBlank(jsonValue)) {
            log.error("[SpringEventBusSendMqServiceImpl#send] jsonValue不能为空, topic:{}", topic);
            throw new IllegalArgumentException("jsonValue不能为空");
        }

        try {
            log.debug("[SpringEventBusSendMqServiceImpl#send] 开始发送消息, topic:{}, tagId:{}, messageLength:{}",
                    topic, tagId, jsonValue.length());

            // 构建事件源
            AustinSpringEventSource source = AustinSpringEventSource.builder()
                    .topic(topic)
                    .jsonValue(jsonValue)
                    .tagId(tagId)
                    .build();

            // 创建并发布事件
            AustinSpringEventBusEvent event = new AustinSpringEventBusEvent(this, source);
            eventPublisher.publishEvent(event);

            log.debug("[SpringEventBusSendMqServiceImpl#send] 消息发送成功, topic:{}, tagId:{}", topic, tagId);
        } catch (Exception e) {
            log.error("[SpringEventBusSendMqServiceImpl#send] 消息发送失败, topic:{}, tagId:{}, error:{}",
                    topic, tagId, e.getMessage(), e);
            throw new RuntimeException("Spring Event Bus消息发送失败", e);
        }
    }

    /**
     * 发送消息到Spring Event Bus（无标签）
     *
     * @param topic     消息主题
     * @param jsonValue 消息内容（JSON格式）
     */
    @Override
    public void send(String topic, String jsonValue) {
        send(topic, jsonValue, null);
    }
}
