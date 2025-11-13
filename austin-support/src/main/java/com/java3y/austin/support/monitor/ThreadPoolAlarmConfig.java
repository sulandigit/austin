package com.java3y.austin.support.monitor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 线程池告警配置
 *
 * @author 3y
 */
@Data
@Component
@ConfigurationProperties(prefix = "austin.thread-pool.alarm")
public class ThreadPoolAlarmConfig {

    /**
     * 是否启用告警，默认启用
     */
    private Boolean enabled = true;

    /**
     * 队列使用率告警阈值（百分比），默认80%
     */
    private Double queueUsageThreshold = 80.0;

    /**
     * 线程池使用率告警阈值（百分比），默认80%
     */
    private Double poolUsageThreshold = 80.0;

    /**
     * 拒绝策略触发告警
     */
    private Boolean rejectAlarmEnabled = true;

    /**
     * 告警间隔时间（秒），防止频繁告警，默认5分钟
     */
    private Long alarmInterval = 300L;

    /**
     * 监控采集间隔（秒），默认30秒
     */
    private Long monitorInterval = 30L;
}
