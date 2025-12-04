package com.java3y.austin.common.domain;

import com.java3y.austin.common.enums.AlertLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MQ积压告警信息
 *
 * @author austin
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MqBacklogAlertInfo {

    /**
     * MQ类型（redis、rabbitmq、kafka等）
     */
    private String mqType;

    /**
     * Topic名称
     */
    private String topic;

    /**
     * 消费组标识
     */
    private String consumerGroup;

    /**
     * 当前积压量
     */
    private Long currentBacklog;

    /**
     * 告警阈值
     */
    private Long thresholdBacklog;

    /**
     * 队列深度/容量（可选）
     */
    private Long queueDepth;

    /**
     * 消费者数量（可选）
     */
    private Integer consumerCount;

    /**
     * 告警级别
     */
    private AlertLevel alertLevel;

    /**
     * 告警产生时间戳
     */
    private Long alertTimestamp;

    /**
     * 扩展上下文信息（如环境、集群名等）
     */
    private String context;
}
