package com.java3y.austin.support.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 智能发送时间优化配置
 *
 * @author austin
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "austin.smart.send.time")
public class SmartSendTimeConfig {

    /**
     * 是否启用智能发送时间优化
     */
    private Boolean enabled = false;

    /**
     * 优化策略：OPEN_RATE（打开率）、CLICK_RATE（点击率）、CONVERSION_RATE（转化率）、COMPREHENSIVE（综合评分）
     */
    private String strategy = "COMPREHENSIVE";

    /**
     * 最小数据样本量（用户历史数据不足时使用全局数据）
     */
    private Integer minSampleSize = 10;

    /**
     * 全局数据最小样本量
     */
    private Integer globalMinSampleSize = 100;

    /**
     * 推荐时间段数量
     */
    private Integer recommendTimeSlots = 3;

    /**
     * 数据有效期（天）
     */
    private Integer dataValidDays = 90;

    /**
     * 是否允许使用全局最佳时间（当用户数据不足时）
     */
    private Boolean allowGlobalBestTime = true;

    /**
     * 综合评分权重配置
     */
    private WeightConfig weight = new WeightConfig();

    /**
     * 时间窗口配置（允许的发送时间范围）
     */
    private TimeWindowConfig timeWindow = new TimeWindowConfig();

    /**
     * 是否启用A/B测试
     */
    private Boolean enableAbTest = false;

    /**
     * A/B测试流量占比（0-100）
     */
    private Integer abTestRatio = 20;

    @Data
    public static class WeightConfig {
        /**
         * 打开率权重
         */
        private Double openRateWeight = 0.4;

        /**
         * 点击率权重
         */
        private Double clickRateWeight = 0.3;

        /**
         * 转化率权重
         */
        private Double conversionRateWeight = 0.2;

        /**
         * 活跃度权重
         */
        private Double activityWeight = 0.1;
    }

    @Data
    public static class TimeWindowConfig {
        /**
         * 允许发送的最早小时（0-23）
         */
        private Integer earliestHour = 8;

        /**
         * 允许发送的最晚小时（0-23）
         */
        private Integer latestHour = 22;

        /**
         * 是否启用时间窗口限制
         */
        private Boolean enabled = true;
    }
}
