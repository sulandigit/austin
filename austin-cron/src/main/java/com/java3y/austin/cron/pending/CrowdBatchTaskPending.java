package com.java3y.austin.cron.pending;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import com.google.common.collect.Lists;
import com.java3y.austin.common.constant.AustinConstant;
import com.java3y.austin.cron.config.CronAsyncThreadPoolConfig;
import com.java3y.austin.cron.constants.PendingConstant;
import com.java3y.austin.cron.vo.CrowdInfoVo;
import com.java3y.austin.service.api.domain.BatchSendRequest;
import com.java3y.austin.service.api.domain.MessageParam;
import com.java3y.austin.service.api.enums.BusinessCode;
import com.java3y.austin.service.api.service.SendService;
import com.java3y.austin.support.pending.AbstractLazyPending;
import com.java3y.austin.support.pending.PendingParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 人群批量任务待处理队列
 * <p>
 * 功能说明：
 * 1. 延迟批量处理人群信息，避免频繁调用发送接口
 * 2. 将相同参数的接收者合并，减少接口调用次数
 * 3. 调用 batch 发送接口进行消息推送
 * <p>
 * 设计模式：
 * - 继承自 AbstractLazyPending，实现延迟批量处理机制
 * - 使用 SCOPE_PROTOTYPE 作用域，每次获取都创建新实例
 *
 * @author 3y
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CrowdBatchTaskPending extends AbstractLazyPending<CrowdInfoVo> {

    /**
     * 消息发送服务
     * 用于批量发送消息到各个接收者
     */
    @Autowired
    private SendService sendService;

    /**
     * 构造函数
     * 初始化待处理队列的相关参数
     */
    public CrowdBatchTaskPending() {
        PendingParam<CrowdInfoVo> pendingParam = new PendingParam<>();
        // 配置队列参数：
        // 1. 设置阻塞队列，容量为 QUEUE_SIZE
        // 2. 设置时间阈值，达到阈值后触发批量处理
        // 3. 设置数量阈值，队列中元素达到该数量后触发批量处理
        // 4. 设置异步线程池，用于消费队列中的数据
        pendingParam.setQueue(new LinkedBlockingQueue<>(PendingConstant.QUEUE_SIZE))
                .setTimeThreshold(PendingConstant.TIME_THRESHOLD)
                .setNumThreshold(AustinConstant.BATCH_RECEIVER_SIZE)
                .setExecutorService(CronAsyncThreadPoolConfig.getConsumePendingThreadPool());
        this.pendingParam = pendingParam;
    }

    /**
     * 处理批量人群信息
     * <p>
     * 核心逻辑：
     * 1. 将具有相同参数的接收者合并在一起，减少消息发送次数
     * 2. 组装成 MessageParam 列表
     * 3. 调用批量发送接口进行消息推送
     *
     * @param crowdInfoVos 待处理的人群信息列表
     */
    @Override
    public void doHandle(List<CrowdInfoVo> crowdInfoVos) {

        // 1. 如果参数相同，组装成同一个 MessageParam 发送
        // Key: 消息参数变量（variables），Value: 接收者列表（用逗号分隔）
        Map<Map<String, String>, String> paramMap = MapUtil.newHashMap();
        for (CrowdInfoVo crowdInfoVo : crowdInfoVos) {
            // 获取接收者
            String receiver = crowdInfoVo.getReceiver();
            // 获取消息参数
            Map<String, String> vars = crowdInfoVo.getParams();
            
            // 如果该参数组合第一次出现，直接添加
            if (Objects.isNull(paramMap.get(vars))) {
                paramMap.put(vars, receiver);
            } else {
                // 如果该参数组合已存在，将新接收者用逗号拼接到已有接收者列表后面
                String newReceiver = StringUtils.join(new String[]{
                        paramMap.get(vars), receiver}, StrPool.COMMA);
                paramMap.put(vars, newReceiver);
            }
        }

        // 2. 组装消息参数列表
        List<MessageParam> messageParams = Lists.newArrayList();
        for (Map.Entry<Map<String, String>, String> entry : paramMap.entrySet()) {
            // 为每组相同参数的接收者创建一个 MessageParam
            MessageParam messageParam = MessageParam.builder().receiver(entry.getValue())
                    .variables(entry.getKey()).build();
            messageParams.add(messageParam);
        }

        // 3. 调用批量发送接口发送消息
        // 构建批量发送请求，包含业务代码、消息参数列表和消息模板 ID
        BatchSendRequest batchSendRequest = BatchSendRequest.builder().code(BusinessCode.COMMON_SEND.getCode())
                .messageParamList(messageParams)
                .messageTemplateId(CollUtil.getFirst(crowdInfoVos.iterator()).getMessageTemplateId())
                .build();
        sendService.batchSend(batchSendRequest);
    }

}
