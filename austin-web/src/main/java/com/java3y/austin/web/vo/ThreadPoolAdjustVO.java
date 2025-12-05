package com.java3y.austin.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 线程池参数调整请求VO
 *
 * @author 3y
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreadPoolAdjustVO {
    
    /**
     * 线程池名称
     */
    @NotBlank(message = "线程池名称不能为空")
    private String threadPoolName;
    
    /**
     * 核心线程数
     */
    @NotNull(message = "核心线程数不能为空")
    @Min(value = 1, message = "核心线程数必须大于0")
    private Integer corePoolSize;
    
    /**
     * 最大线程数
     */
    @NotNull(message = "最大线程数不能为空")
    @Min(value = 1, message = "最大线程数必须大于0")
    private Integer maximumPoolSize;
    
    /**
     * 队列容量
     */
    @Min(value = 0, message = "队列容量不能为负数")
    private Integer queueCapacity;
    
    /**
     * 线程存活时间(秒)
     */
    @Min(value = 0, message = "线程存活时间不能为负数")
    private Long keepAliveTime;
    
    /**
     * 拒绝策略
     */
    private String rejectedHandlerType;
}
