package com.java3y.austin.support.monitor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 线程池告警规则配置
 * 支持通过配置文件自定义告警阈值和规则
 *
 * @author austin
 */
@Data
@Component
@ConfigurationProperties(prefix = "austin.thread-pool.alarm")
public class ThreadPoolAlarmRuleConfig {

    /**
     * 是否启用告警
     */
    private Boolean enabled = true;

    /**
     * 告警检查间隔（秒）
     */
    private Integer checkInterval = 30;

    /**
     * 队列使用率告警阈值（百分比）
     */
    private Integer queueUsageThreshold = 80;

    /**
     * 线程池活跃度告警阈值（百分比）
     */
    private Integer livenessThreshold = 80;

    /**
     * 拒绝任务数告警阈值
     */
    private Long rejectCountThreshold = 10L;

    /**
     * 告警冷却时间（秒），同一告警在冷却期内不重复发送
     */
    private Integer cooldownPeriod = 300;

    /**
     * 线程池特定配置，key为线程池名称
     */
    private Map<String, ThreadPoolSpecificRule> specificRules = new HashMap<>();

    /**
     * 线程池特定告警规则
     */
    @Data
    public static class ThreadPoolSpecificRule {
        /**
         * 队列使用率阈值
         */
        private Integer queueUsageThreshold;

        /**
         * 活跃度阈值
         */
        private Integer livenessThreshold;

        /**
         * 拒绝任务数阈值
         */
        private Long rejectCountThreshold;

        /**
         * 是否启用该线程池的告警
         */
        private Boolean enabled = true;
    }

    /**
     * 获取指定线程池的队列使用率阈值
     */
    public Integer getQueueUsageThreshold(String poolName) {
        ThreadPoolSpecificRule rule = specificRules.get(poolName);
        if (rule != null && rule.getQueueUsageThreshold() != null) {
            return rule.getQueueUsageThreshold();
        }
        return queueUsageThreshold;
    }

    /**
     * 获取指定线程池的活跃度阈值
     */
    public Integer getLivenessThreshold(String poolName) {
        ThreadPoolSpecificRule rule = specificRules.get(poolName);
        if (rule != null && rule.getLivenessThreshold() != null) {
            return rule.getLivenessThreshold();
        }
        return livenessThreshold;
    }

    /**
     * 获取指定线程池的拒绝任务数阈值
     */
    public Long getRejectCountThreshold(String poolName) {
        ThreadPoolSpecificRule rule = specificRules.get(poolName);
        if (rule != null && rule.getRejectCountThreshold() != null) {
            return rule.getRejectCountThreshold();
        }
        return rejectCountThreshold;
    }

    /**
     * 判断指定线程池是否启用告警
     */
    public Boolean isPoolAlarmEnabled(String poolName) {
        if (!enabled) {
            return false;
        }
        ThreadPoolSpecificRule rule = specificRules.get(poolName);
        return rule == null || rule.getEnabled();
    }
}
