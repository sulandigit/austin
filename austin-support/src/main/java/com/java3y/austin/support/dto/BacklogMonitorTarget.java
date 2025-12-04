package com.java3y.austin.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MQ积压监控目标配置
 *
 * @author austin
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BacklogMonitorTarget {

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
     * 警告阈值（覆盖全局默认值）
     */
    private Long warnThreshold;

    /**
     * 严重阈值（覆盖全局默认值）
     */
    private Long criticalThreshold;

    /**
     * 是否启用监控
     */
    private Boolean enabled;
}
