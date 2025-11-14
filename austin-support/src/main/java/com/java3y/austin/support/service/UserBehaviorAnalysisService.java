package com.java3y.austin.support.service;

import com.java3y.austin.support.domain.UserBehaviorStats;

import java.util.List;

/**
 * 用户行为分析服务接口
 *
 * @author austin
 */
public interface UserBehaviorAnalysisService {

    /**
     * 更新用户行为统计数据
     *
     * @param receiver     接收者
     * @param sendChannel  发送渠道
     * @param hourOfDay    发送时段（0-23）
     * @param isOpen       是否打开
     * @param isClick      是否点击
     * @param isConversion 是否转化
     */
    void updateBehaviorStats(String receiver, Integer sendChannel, Integer hourOfDay,
                            boolean isOpen, boolean isClick, boolean isConversion);

    /**
     * 批量更新用户行为统计
     *
     * @param statsList 统计数据列表
     */
    void batchUpdateBehaviorStats(List<UserBehaviorStats> statsList);

    /**
     * 获取用户在指定渠道的行为统计
     *
     * @param receiver    接收者
     * @param sendChannel 发送渠道
     * @return 用户行为统计列表
     */
    List<UserBehaviorStats> getUserBehaviorStats(String receiver, Integer sendChannel);

    /**
     * 计算用户活跃度评分
     *
     * @param receiver    接收者
     * @param sendChannel 发送渠道
     * @return 活跃度评分（0-100）
     */
    Integer calculateActivityScore(String receiver, Integer sendChannel);

    /**
     * 清理过期数据
     *
     * @param days 数据保留天数
     */
    void cleanExpiredData(Integer days);
}
