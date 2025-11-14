package com.java3y.austin.support.service.impl;

import com.java3y.austin.support.dao.UserBehaviorStatsDao;
import com.java3y.austin.support.domain.UserBehaviorStats;
import com.java3y.austin.support.service.UserBehaviorAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户行为分析服务实现
 *
 * @author austin
 */
@Slf4j
@Service
public class UserBehaviorAnalysisServiceImpl implements UserBehaviorAnalysisService {

    @Autowired
    private UserBehaviorStatsDao userBehaviorStatsDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBehaviorStats(String receiver, Integer sendChannel, Integer hourOfDay,
                                   boolean isOpen, boolean isClick, boolean isConversion) {
        try {
            // 查询现有统计数据
            UserBehaviorStats stats = userBehaviorStatsDao.findByReceiverAndSendChannelAndHourOfDay(
                    receiver, sendChannel, hourOfDay);

            long currentTime = System.currentTimeMillis() / 1000;

            if (stats == null) {
                // 创建新的统计记录
                stats = UserBehaviorStats.builder()
                        .receiver(receiver)
                        .sendChannel(sendChannel)
                        .hourOfDay(hourOfDay)
                        .sendCount(1L)
                        .openCount(isOpen ? 1L : 0L)
                        .clickCount(isClick ? 1L : 0L)
                        .conversionCount(isConversion ? 1L : 0L)
                        .avgOpenDuration(0L)
                        .created(currentTime)
                        .updated(currentTime)
                        .lastUpdateTime(currentTime)
                        .build();
            } else {
                // 更新现有统计记录
                stats.setSendCount(stats.getSendCount() + 1);
                if (isOpen) {
                    stats.setOpenCount(stats.getOpenCount() + 1);
                }
                if (isClick) {
                    stats.setClickCount(stats.getClickCount() + 1);
                }
                if (isConversion) {
                    stats.setConversionCount(stats.getConversionCount() + 1);
                }
                stats.setUpdated(currentTime);
                stats.setLastUpdateTime(currentTime);
            }

            // 计算各项指标
            calculateMetrics(stats);

            // 保存统计数据
            userBehaviorStatsDao.save(stats);

            log.info("UserBehaviorAnalysisService#updateBehaviorStats success, receiver:{}, channel:{}, hour:{}",
                    receiver, sendChannel, hourOfDay);
        } catch (Exception e) {
            log.error("UserBehaviorAnalysisService#updateBehaviorStats error, receiver:{}, channel:{}, hour:{}, error:{}",
                    receiver, sendChannel, hourOfDay, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateBehaviorStats(List<UserBehaviorStats> statsList) {
        try {
            statsList.forEach(this::calculateMetrics);
            userBehaviorStatsDao.saveAll(statsList);
            log.info("UserBehaviorAnalysisService#batchUpdateBehaviorStats success, size:{}", statsList.size());
        } catch (Exception e) {
            log.error("UserBehaviorAnalysisService#batchUpdateBehaviorStats error:{}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<UserBehaviorStats> getUserBehaviorStats(String receiver, Integer sendChannel) {
        return userBehaviorStatsDao.findByReceiverAndSendChannel(receiver, sendChannel);
    }

    @Override
    public Integer calculateActivityScore(String receiver, Integer sendChannel) {
        List<UserBehaviorStats> statsList = getUserBehaviorStats(receiver, sendChannel);
        if (statsList == null || statsList.isEmpty()) {
            return 0;
        }

        // 计算综合活跃度评分
        long totalSend = 0;
        long totalOpen = 0;
        long totalClick = 0;
        long totalConversion = 0;

        for (UserBehaviorStats stats : statsList) {
            totalSend += stats.getSendCount();
            totalOpen += stats.getOpenCount();
            totalClick += stats.getClickCount();
            totalConversion += stats.getConversionCount();
        }

        if (totalSend == 0) {
            return 0;
        }

        // 综合评分计算：打开率40%，点击率30%，转化率30%
        double openRate = (totalOpen * 100.0) / totalSend;
        double clickRate = (totalClick * 100.0) / totalSend;
        double conversionRate = (totalConversion * 100.0) / totalSend;

        int score = (int) (openRate * 0.4 + clickRate * 0.3 + conversionRate * 0.3);
        return Math.min(100, Math.max(0, score));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredData(Integer days) {
        try {
            long expiredTime = System.currentTimeMillis() / 1000 - (days * 24 * 3600L);
            // 这里需要添加自定义查询来删除过期数据
            log.info("UserBehaviorAnalysisService#cleanExpiredData, days:{}, expiredTime:{}", days, expiredTime);
        } catch (Exception e) {
            log.error("UserBehaviorAnalysisService#cleanExpiredData error:{}", e.getMessage());
        }
    }

    /**
     * 计算统计指标
     */
    private void calculateMetrics(UserBehaviorStats stats) {
        if (stats.getSendCount() > 0) {
            // 计算打开率
            stats.setOpenRate((stats.getOpenCount() * 100.0) / stats.getSendCount());
            // 计算点击率
            stats.setClickRate((stats.getClickCount() * 100.0) / stats.getSendCount());
            // 计算转化率
            stats.setConversionRate((stats.getConversionCount() * 100.0) / stats.getSendCount());
        } else {
            stats.setOpenRate(0.0);
            stats.setClickRate(0.0);
            stats.setConversionRate(0.0);
        }

        // 计算活跃度评分（单个时段）
        int score = (int) (stats.getOpenRate() * 0.4 + stats.getClickRate() * 0.3 + stats.getConversionRate() * 0.3);
        stats.setActivityScore(Math.min(100, Math.max(0, score)));
    }
}
