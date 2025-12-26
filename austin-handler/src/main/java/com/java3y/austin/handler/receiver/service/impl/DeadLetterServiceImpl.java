package com.java3y.austin.handler.receiver.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.java3y.austin.common.domain.AnchorInfo;
import com.java3y.austin.common.domain.DeadLetterMessage;
import com.java3y.austin.common.domain.LogParam;
import com.java3y.austin.common.domain.RecallTaskInfo;
import com.java3y.austin.common.domain.TaskInfo;
import com.java3y.austin.common.enums.AnchorState;
import com.java3y.austin.handler.receiver.service.DeadLetterService;
import com.java3y.austin.support.dao.DeadLetterMessageDao;
import com.java3y.austin.support.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 死信队列处理服务实现
 *
 * @author austin
 */
@Slf4j
@Service
public class DeadLetterServiceImpl implements DeadLetterService {

    private static final String LOG_BIZ_TYPE = "DeadLetter#handle";

    @Autowired
    private LogUtils logUtils;

    @Autowired(required = false)
    private DeadLetterMessageDao deadLetterMessageDao;

    @Override
    public void handleDeadLetterSend(List<TaskInfo> taskInfoLists, Message message) {
        if (CollUtil.isEmpty(taskInfoLists)) {
            log.warn("死信队列收到空任务列表");
            return;
        }

        try {
            // 记录死信信息
            logDeadLetterMessage(message, "SEND");

            // 记录埋点日志 - 消息进入死信队列
            for (TaskInfo taskInfo : taskInfoLists) {
                LogParam logParam = LogParam.builder()
                        .bizType(LOG_BIZ_TYPE)
                        .object(taskInfo)
                        .build();
                logUtils.print(AnchorInfo.builder()
                        .businessId(taskInfo.getBusinessId())
                        .ids(taskInfo.getReceiver())
                        .state(AnchorState.DEAD_LETTER.getCode())
                        .logParam(logParam)
                        .build());

                log.error("消息进入死信队列，businessId: {}, messageTemplateId: {}, receiver: {}, 原因: {}",
                        taskInfo.getBusinessId(),
                        taskInfo.getMessageTemplateId(),
                        taskInfo.getReceiver(),
                        getDeadLetterReason(message));
            }

            // TODO: 可以在这里实现以下策略：
            // 1. 将死信消息持久化到数据库，便于后续人工处理或重试
            saveDeadLetterToDatabase(taskInfoLists, message, "SEND");
            // 2. 发送告警通知给相关负责人
            // 3. 根据业务需要进行补偿处理
            // 4. 统计死信消息数量，用于监控告警

        } catch (Exception e) {
            log.error("处理发送消息死信异常, taskInfoLists: {}", JSON.toJSONString(taskInfoLists), e);
        }
    }

    @Override
    public void handleDeadLetterRecall(RecallTaskInfo recallTaskInfo, Message message) {
        if (recallTaskInfo == null) {
            log.warn("死信队列收到空撤回任务");
            return;
        }

        try {
            // 记录死信信息
            logDeadLetterMessage(message, "RECALL");

            log.error("撤回消息进入死信队列，messageTemplateId: {}, recallMessageId: {}, 原因: {}",
                    recallTaskInfo.getMessageTemplateId(),
                    recallTaskInfo.getRecallMessageId(),
                    getDeadLetterReason(message));

            // 持久化到数据库
            saveDeadLetterToDatabase(recallTaskInfo, message, "RECALL");
            // TODO: 可以在这里实现撤回消息的补偿策略

        } catch (Exception e) {
            log.error("处理撤回消息死信异常, recallTaskInfo: {}", JSON.toJSONString(recallTaskInfo), e);
        }
    }

    @Override
    public void handleDeadLetterException(Message message, Exception exception) {
        try {
            String messageContent = new String(message.getBody(), StandardCharsets.UTF_8);
            log.error("死信消息处理异常，消息内容: {}, 异常信息: {}", messageContent, exception.getMessage(), exception);

            // TODO: 记录到数据库或发送告警
            // 可以将无法处理的死信消息存储到特殊表中，便于后续排查

        } catch (Exception e) {
            log.error("处理死信异常时发生错误", e);
        }
    }

    /**
     * 记录死信消息详细信息
     */
    private void logDeadLetterMessage(Message message, String messageType) {
        try {
            MessageProperties properties = message.getMessageProperties();
            String messageContent = new String(message.getBody(), StandardCharsets.UTF_8);

            log.error("=== 死信队列详细信息 ===");
            log.error("消息类型: {}", messageType);
            log.error("消息内容: {}", messageContent);
            log.error("死信原因: {}", getDeadLetterReason(message));
            log.error("消息ID: {}", properties.getMessageId());
            log.error("关联ID: {}", properties.getCorrelationId());
            log.error("时间戳: {}", properties.getTimestamp());
            log.error("重试次数: {}", getRetryCount(message));
            log.error("原始Exchange: {}", properties.getReceivedExchange());
            log.error("原始RoutingKey: {}", properties.getReceivedRoutingKey());
            log.error("========================");

        } catch (Exception e) {
            log.error("记录死信消息信息失败", e);
        }
    }

    /**
     * 获取死信原因
     */
    private String getDeadLetterReason(Message message) {
        MessageProperties properties = message.getMessageProperties();
        Map<String, Object> headers = properties.getHeaders();

        // x-death 头包含了消息变成死信的详细信息
        if (headers.containsKey("x-death")) {
            return "详情见x-death: " + headers.get("x-death");
        }

        // x-first-death-reason 包含第一次死信的原因
        if (headers.containsKey("x-first-death-reason")) {
            return headers.get("x-first-death-reason").toString();
        }

        // 常见的死信原因
        if (headers.containsKey("x-first-death-exchange")) {
            return "消息被拒绝、过期或队列已满";
        }

        return "未知原因";
    }

    /**
     * 获取消息重试次数
     */
    private int getRetryCount(Message message) {
        MessageProperties properties = message.getMessageProperties();
        Map<String, Object> headers = properties.getHeaders();

        if (headers.containsKey("x-death")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> xDeathList = (List<Map<String, Object>>) headers.get("x-death");
            if (CollUtil.isNotEmpty(xDeathList)) {
                Map<String, Object> firstDeath = xDeathList.get(0);
                Object count = firstDeath.get("count");
                if (count != null) {
                    return Integer.parseInt(count.toString());
                }
            }
        }

        return 0;
    }

    /**
     * 保存死信消息到数据库 - 发送消息
     */
    private void saveDeadLetterToDatabase(List<TaskInfo> taskInfoLists, Message message, String messageType) {
        if (deadLetterMessageDao == null) {
            log.warn("DeadLetterMessageDao未注入，跳过数据库保存");
            return;
        }

        try {
            MessageProperties properties = message.getMessageProperties();
            String messageContent = new String(message.getBody(), StandardCharsets.UTF_8);

            for (TaskInfo taskInfo : taskInfoLists) {
                DeadLetterMessage deadLetterMessage = DeadLetterMessage.builder()
                        .messageType(messageType)
                        .messageContent(messageContent)
                        .businessId(taskInfo.getBusinessId())
                        .messageTemplateId(taskInfo.getMessageTemplateId())
                        .deadLetterReason(getDeadLetterReason(message))
                        .retryCount(getRetryCount(message))
                        .originalExchange(properties.getReceivedExchange())
                        .originalRoutingKey(properties.getReceivedRoutingKey())
                        .messageId(properties.getMessageId())
                        .correlationId(properties.getCorrelationId())
                        .handleStatus(0) // 0-未处理
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .extraInfo(JSON.toJSONString(taskInfo))
                        .build();

                deadLetterMessageDao.save(deadLetterMessage);
                log.info("死信消息已保存到数据库，businessId: {}", taskInfo.getBusinessId());
            }
        } catch (Exception e) {
            log.error("保存死信消息到数据库失败", e);
        }
    }

    /**
     * 保存死信消息到数据库 - 撤回消息
     */
    private void saveDeadLetterToDatabase(RecallTaskInfo recallTaskInfo, Message message, String messageType) {
        if (deadLetterMessageDao == null) {
            log.warn("DeadLetterMessageDao未注入，跳过数据库保存");
            return;
        }

        try {
            MessageProperties properties = message.getMessageProperties();
            String messageContent = new String(message.getBody(), StandardCharsets.UTF_8);

            DeadLetterMessage deadLetterMessage = DeadLetterMessage.builder()
                    .messageType(messageType)
                    .messageContent(messageContent)
                    .messageTemplateId(recallTaskInfo.getMessageTemplateId())
                    .deadLetterReason(getDeadLetterReason(message))
                    .retryCount(getRetryCount(message))
                    .originalExchange(properties.getReceivedExchange())
                    .originalRoutingKey(properties.getReceivedRoutingKey())
                    .messageId(properties.getMessageId())
                    .correlationId(properties.getCorrelationId())
                    .handleStatus(0) // 0-未处理
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .extraInfo(JSON.toJSONString(recallTaskInfo))
                    .build();

            deadLetterMessageDao.save(deadLetterMessage);
            log.info("撤回消息死信已保存到数据库，messageTemplateId: {}", recallTaskInfo.getMessageTemplateId());
        } catch (Exception e) {
            log.error("保存撤回消息死信到数据库失败", e);
        }
    }
}
