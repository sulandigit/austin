package com.java3y.austin.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SimpleTaskInfo - Information returned after successfully calling the sending interface, used to check the delivery status
 * SimpleTaskInfo  调用发送接口成功后返回对应的信息，用于查看下发情况
 * 
 * @Author: sky
 * @Date: 2023/7/13 10:43
 * @Version 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleTaskInfo {

    /**
     * Business message sending ID, used for link tracing. If not exist, use messageId instead
     * 业务消息发送Id, 用于链路追踪, 若不存在, 则使用 messageId
     */
    private String bizId;

    /**
     * Unique message ID (used for data tracking)
     * 消息唯一Id(数据追踪使用)
     * Generation logic reference: TaskInfoUtils
     * 生成逻辑参考 TaskInfoUtils
     */
    private String messageId;

    /**
     * Business ID (used for data tracking)
     * 业务Id(数据追踪使用)
     * Generation logic reference: TaskInfoUtils
     * 生成逻辑参考 TaskInfoUtils
     */
    private Long businessId;
}
