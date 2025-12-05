package com.java3y.austin.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 线程池信息VO
 *
 * @author 3y
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreadPoolVO {
    
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
     * 队列容量
     */
    private Integer queueCapacity;
    
    /**
     * 队列类型
     */
    private String queueType;
    
    /**
     * 拒绝策略
     */
    private String rejectedHandlerType;
    
    /**
     * 线程存活时间(秒)
     */
    private Long keepAliveTime;
    
    /**
     * 是否允许核心线程超时
     */
    private Boolean allowCoreThreadTimeOut;
    
    /**
     * 活跃线程数
     */
    private Integer activeCount;
    
    /**
     * 当前线程数
     */
    private Integer poolSize;
    
    /**
     * 最大线程数(历史)
     */
    private Integer largestPoolSize;
    
    /**
     * 已完成任务数
     */
    private Long completedTaskCount;
    
    /**
     * 队列中等待任务数
     */
    private Integer queueSize;
    
    /**
     * 队列剩余容量
     */
    private Integer queueRemainingCapacity;
    
    /**
     * 拒绝任务数
     */
    private Long rejectCount;
}
