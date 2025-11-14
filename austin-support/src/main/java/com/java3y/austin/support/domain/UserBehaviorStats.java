package com.java3y.austin.support.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 用户行为统计实体
 * 用于记录用户的消息接收、打开、点击等行为数据，支持智能发送时间分析
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Accessors(chain = true)
@Table(name = "user_behavior_stats", indexes = {
        @Index(name = "idx_receiver_channel", columnList = "receiver,sendChannel"),
        @Index(name = "idx_hour", columnList = "hourOfDay")
})
public class UserBehaviorStats implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 接收者ID（手机号、邮箱、userId等）
     */
    private String receiver;

    /**
     * 发送渠道（10:IM 20:Push 30:短信 40:Email 50:公众号 60:小程序 70:企业微信 80:钉钉机器人 90:钉钉工作通知 100:企业微信机器人 110:飞书机器人）
     */
    private Integer sendChannel;

    /**
     * 统计日期的小时数（0-23）
     */
    private Integer hourOfDay;

    /**
     * 消息发送总数
     */
    private Long sendCount;

    /**
     * 消息打开总数
     */
    private Long openCount;

    /**
     * 消息点击总数
     */
    private Long clickCount;

    /**
     * 转化总数（业务定义的有效转化）
     */
    private Long conversionCount;

    /**
     * 平均打开时长（秒）
     */
    private Long avgOpenDuration;

    /**
     * 打开率（百分比，保留2位小数）
     */
    private Double openRate;

    /**
     * 点击率（百分比，保留2位小数）
     */
    private Double clickRate;

    /**
     * 转化率（百分比，保留2位小数）
     */
    private Double conversionRate;

    /**
     * 用户活跃度评分（0-100）
     */
    private Integer activityScore;

    /**
     * 最后更新时间（时间戳，秒）
     */
    private Long lastUpdateTime;

    /**
     * 创建时间（时间戳，秒）
     */
    private Long created;

    /**
     * 更新时间（时间戳，秒）
     */
    private Long updated;
}
