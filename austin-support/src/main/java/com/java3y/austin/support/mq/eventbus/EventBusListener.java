package com.java3y.austin.support.mq.eventbus;


import com.java3y.austin.common.domain.RecallTaskInfo;
import com.java3y.austin.common.domain.TaskInfo;

import java.util.List;

/**
 * EventBus 事件监听器接口
 * <p>
 * 该接口定义了基于 EventBus 的消息监听和处理规范，主要用于异步处理消息发送和撤回任务。
 * 实现类需要提供具体的消息消费和撤回逻辑。
 * </p>
 *
 * @author 3y
 */
public interface EventBusListener {


    /**
     * 消费消息
     * <p>
     * 处理消息发送任务列表，将任务分发到对应的消息发送渠道进行处理。
     * 该方法会在接收到消息发送事件时被调用。
     * </p>
     *
     * @param lists 待处理的任务信息列表，包含消息发送所需的所有信息
     */
    void consume(List<TaskInfo> lists);

    /**
     * 撤回消息
     * <p>
     * 处理消息撤回任务，根据撤回信息执行相应的撤回操作。
     * 该方法会在接收到消息撤回事件时被调用。
     * </p>
     *
     * @param recallTaskInfo 撤回任务信息，包含需要撤回的消息标识和相关参数
     */
    void recall(RecallTaskInfo recallTaskInfo);
}
