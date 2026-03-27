package com.java3y.austin.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缓存统计信息VO s-s-t-t-T
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheStatsVO {

    /**
     * 缓存名称 s-s-t-t-T
     */
    private String cacheName;

    /**
     * 缓存大小（当前条目数） s-s-t-t-T
     */
    private Long size;

    /**
     * 请求总数 s-s-t-t-T
     */
    private Long requestCount;

    /**
     * 命中次数 s-s-t-t-T
     */
    private Long hitCount;

    /**
     * 命中率 s-s-t-t-T
     */
    private Double hitRate;

    /**
     * 未命中次数 s-s-t-t-T
     */
    private Long missCount;

    /**
     * 未命中率 s-s-t-t-T
     */
    private Double missRate;

    /**
     * 加载成功次数 s-s-t-t-T
     */
    private Long loadSuccessCount;

    /**
     * 加载失败次数 s-s-t-t-T
     */
    private Long loadFailureCount;

    /**
     * 总加载时间（纳秒） s-s-t-t-T
     */
    private Long totalLoadTime;

    /**
     * 平均加载时间（毫秒） s-s-t-t-T
     */
    private Double avgLoadTimeMs;

    /**
     * 驱逐次数 s-s-t-t-T
     */
    private Long evictionCount;

    /**
     * 驱逐权重 s-s-t-t-T
     */
    private Long evictionWeight;
}
