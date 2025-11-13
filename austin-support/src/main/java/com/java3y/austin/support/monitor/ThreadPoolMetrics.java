package com.java3y.austin.support.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 线程池监控指标
 *
 * @author 3y
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolMetrics implements Serializable {

    /**
     * 线程池名称
     */
    private String poolName;

    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    private Integer maximumPoolSize;

    /**
     * 当前线程数
     */
    private Integer poolSize;

    /**
     * 活跃线程数
     */
    private Integer activeCount;

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
     * 历史最大线程数
     */
    private Integer largestPoolSize;

    /**
     * 已完成任务数
     */
    private Long completedTaskCount;

    /**
     * 总任务数
     */
    private Long taskCount;

    /**
     * 队列使用率
     */
    private Double queueUsageRate;

    /**
     * 线程池使用率
     */
    private Double poolUsageRate;

    /**
     * 是否拒绝任务
     */
    private Boolean rejected;

    /**
     * 采集时间
     */
    private Date collectTime;
}
