package com.java3y.austin.support.service;

import com.java3y.austin.support.dto.SmartSendTimeResult;

import java.util.List;

/**
 * 智能发送时间优化服务接口
 *
 * @author austin
 */
public interface SmartSendTimeOptimizer {

    /**
     * 获取用户的最佳发送时间
     *
     * @param receiver    接收者
     * @param sendChannel 发送渠道
     * @return 最佳发送时间（小时，0-23）
     */
    Integer getBestSendTime(String receiver, Integer sendChannel);

    /**
     * 获取用户的推荐发送时间列表
     *
     * @param receiver    接收者
     * @param sendChannel 发送渠道
     * @param topN        返回前N个推荐时间
     * @return 推荐发送时间列表
     */
    List<SmartSendTimeResult> getRecommendedSendTimes(String receiver, Integer sendChannel, Integer topN);

    /**
     * 批量获取多个用户的最佳发送时间
     *
     * @param receivers   接收者列表
     * @param sendChannel 发送渠道
     * @return 用户-最佳发送时间映射
     */
    List<SmartSendTimeResult> batchGetBestSendTime(List<String> receivers, Integer sendChannel);

    /**
     * 获取全局最佳发送时间（用于新用户）
     *
     * @param sendChannel 发送渠道
     * @return 全局最佳发送时间（小时，0-23）
     */
    Integer getGlobalBestSendTime(Integer sendChannel);

    /**
     * 判断指定时间是否为用户的最佳发送时间
     *
     * @param receiver    接收者
     * @param sendChannel 发送渠道
     * @param hourOfDay   指定时间（小时，0-23）
     * @return 是否为最佳发送时间
     */
    Boolean isBestSendTime(String receiver, Integer sendChannel, Integer hourOfDay);

    /**
     * 优化发送时间（将给定时间调整为最佳时间）
     *
     * @param receiver      接收者
     * @param sendChannel   发送渠道
     * @param originalTime  原始发送时间（时间戳，毫秒）
     * @return 优化后的发送时间（时间戳，毫秒）
     */
    Long optimizeSendTime(String receiver, Integer sendChannel, Long originalTime);
}
