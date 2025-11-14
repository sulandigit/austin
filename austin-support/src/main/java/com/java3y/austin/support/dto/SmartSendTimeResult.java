package com.java3y.austin.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能发送时间结果DTO
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartSendTimeResult {

    /**
     * 接收者
     */
    private String receiver;

    /**
     * 发送渠道
     */
    private Integer sendChannel;

    /**
     * 推荐的发送时间（小时，0-23）
     */
    private Integer hourOfDay;

    /**
     * 预测打开率
     */
    private Double predictedOpenRate;

    /**
     * 预测点击率
     */
    private Double predictedClickRate;

    /**
     * 预测转化率
     */
    private Double predictedConversionRate;

    /**
     * 综合评分（0-100）
     */
    private Double comprehensiveScore;

    /**
     * 数据来源：USER（用户数据）、GLOBAL（全局数据）
     */
    private String dataSource;

    /**
     * 样本量
     */
    private Long sampleSize;

    /**
     * 置信度（0-100）
     */
    private Double confidence;

    /**
     * 是否为最佳时间
     */
    private Boolean isBestTime;

    /**
     * 排名（在推荐列表中的排名）
     */
    private Integer rank;
}
