package com.java3y.austin.handler.receiver.service;

import com.java3y.austin.common.domain.RecallTaskInfo;
import com.java3y.austin.common.domain.TaskInfo;
import org.springframework.amqp.core.Message;

import java.util.List;

/**
 * 死信队列处理服务
 *
 * @author austin
 */
public interface DeadLetterService {

    /**
     * 处理发送消息的死信
     *
     * @param taskInfoLists 任务信息列表
     * @param message       原始消息
     */
    void handleDeadLetterSend(List<TaskInfo> taskInfoLists, Message message);

    /**
     * 处理撤回消息的死信
     *
     * @param recallTaskInfo 撤回任务信息
     * @param message        原始消息
     */
    void handleDeadLetterRecall(RecallTaskInfo recallTaskInfo, Message message);

    /**
     * 处理死信消息异常
     *
     * @param message   原始消息
     * @param exception 异常信息
     */
    void handleDeadLetterException(Message message, Exception exception);
}
