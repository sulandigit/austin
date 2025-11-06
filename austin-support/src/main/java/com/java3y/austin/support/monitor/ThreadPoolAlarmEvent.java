package com.java3y.austin.support.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 线程池告警事件
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolAlarmEvent {

    /**
     * 告警类型
     */
    private AlarmType alarmType;

    /**
     * 线程池名称
     */
    private String threadPoolName;

    /**
     * 告警标题
     */
    private String title;

    /**
     * 告警消息
     */
    private String message;

    /**
     * 当前值
     */
    private String currentValue;

    /**
     * 阈值
     */
    private String threshold;

    /**
     * 监控指标详情
     */
    private ThreadPoolMonitorMetrics metrics;

    /**
     * 告警时间
     */
    private Long timestamp;

    /**
     * 告警级别
     */
    private AlarmLevel level;

    /**
     * 告警类型枚举
     */
    public enum AlarmType {
        /**
         * 队列使用率过高
         */
        QUEUE_USAGE_HIGH("队列使用率过高"),
        
        /**
         * 线程池活跃度过高
         */
        LIVENESS_HIGH("线程池活跃度过高"),
        
        /**
         * 拒绝任务数过多
         */
        REJECT_COUNT_HIGH("拒绝任务数过多"),
        
        /**
         * 线程池配置变更
         */
        CONFIG_CHANGE("线程池配置变更");

        private final String description;

        AlarmType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 告警级别枚举
     */
    public enum AlarmLevel {
        /**
         * 警告
         */
        WARNING("警告"),
        
        /**
         * 严重
         */
        CRITICAL("严重"),
        
        /**
         * 紧急
         */
        URGENT("紧急");

        private final String description;

        AlarmLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
