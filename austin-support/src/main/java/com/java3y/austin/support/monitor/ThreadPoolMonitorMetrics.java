package com.java3y.austin.support.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 线程池监控指标数据
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolMonitorMetrics {

    /**
     * 线程池名称
     */
    private String threadPoolName;

    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    private Integer maximumPoolSize;

    /**
     * 当前活跃线程数
     */
    private Integer activeCount;

    /**
     * 当前线程池大小
     */
    private Integer poolSize;

    /**
     * 最大线程池大小（历史峰值）
     */
    private Integer largestPoolSize;

    /**
     * 队列类型
     */
    private String queueType;

    /**
     * 队列容量
     */
    private Integer queueCapacity;

    /**
     * 队列当前大小
     */
    private Integer queueSize;

    /**
     * 队列剩余容量
     */
    private Integer queueRemainingCapacity;

    /**
     * 已完成任务数
     */
    private Long completedTaskCount;

    /**
     * 总任务数
     */
    private Long taskCount;

    /**
     * 拒绝策略类型
     */
    private String rejectedHandlerType;

    /**
     * 拒绝任务数量
     */
    private Long rejectCount;

    /**
     * 线程池活跃度（活跃线程数/最大线程数）
     */
    private Double liveness;

    /**
     * 队列使用率
     */
    private Double queueUsageRate;

    /**
     * 监控数据采集时间
     */
    private Long timestamp;
}
