package com.java3y.austin.support.service.impl;

import com.java3y.austin.support.config.SmartSendTimeConfig;
import com.java3y.austin.support.dao.UserBehaviorStatsDao;
import com.java3y.austin.support.domain.UserBehaviorStats;
import com.java3y.austin.support.dto.SmartSendTimeResult;
import com.java3y.austin.support.service.SmartSendTimeOptimizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能发送时间优化服务实现
 *
 * @author austin
 */
@Slf4j
@Service
public class SmartSendTimeOptimizerImpl implements SmartSendTimeOptimizer {

    @Autowired
    private UserBehaviorStatsDao userBehaviorStatsDao;

    @Autowired
    private SmartSendTimeConfig smartSendTimeConfig;

    @Override
    public Integer getBestSendTime(String receiver, Integer sendChannel) {
        if (!smartSendTimeConfig.getEnabled()) {
            log.info("SmartSendTimeOptimizer is disabled, return null");
            return null;
        }

        List<SmartSendTimeResult> recommendedTimes = getRecommendedSendTimes(receiver, sendChannel, 1);
        if (recommendedTimes == null || recommendedTimes.isEmpty()) {
            return getGlobalBestSendTime(sendChannel);
        }

        return recommendedTimes.get(0).getHourOfDay();
    }

    @Override
    public List<SmartSendTimeResult> getRecommendedSendTimes(String receiver, Integer sendChannel, Integer topN) {
        if (!smartSendTimeConfig.getEnabled()) {
            log.info("SmartSendTimeOptimizer is disabled, return empty list");
            return Collections.emptyList();
        }

        List<UserBehaviorStats> userStats = userBehaviorStatsDao.findByReceiverAndSendChannel(receiver, sendChannel);

        // 检查用户数据是否足够
        long totalSamples = userStats.stream().mapToLong(UserBehaviorStats::getSendCount).sum();
        if (totalSamples < smartSendTimeConfig.getMinSampleSize()) {
            log.info("User data insufficient, samples:{}, using global data", totalSamples);
            return getGlobalRecommendedSendTimes(sendChannel, topN);
        }

        // 根据策略排序
        List<SmartSendTimeResult> results = convertToResults(userStats, receiver, sendChannel, "USER");
        results = sortByStrategy(results);

        // 应用时间窗口过滤
        if (smartSendTimeConfig.getTimeWindow().getEnabled()) {
            results = filterByTimeWindow(results);
        }

        // 设置排名和最佳时间标识
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setRank(i + 1);
            results.get(i).setIsBestTime(i == 0);
        }

        return results.stream().limit(topN).collect(Collectors.toList());
    }

    @Override
    public List<SmartSendTimeResult> batchGetBestSendTime(List<String> receivers, Integer sendChannel) {
        if (!smartSendTimeConfig.getEnabled()) {
            return Collections.emptyList();
        }

        List<SmartSendTimeResult> results = new ArrayList<>();
        for (String receiver : receivers) {
            Integer bestTime = getBestSendTime(receiver, sendChannel);
            if (bestTime != null) {
                SmartSendTimeResult result = SmartSendTimeResult.builder()
                        .receiver(receiver)
                        .sendChannel(sendChannel)
                        .hourOfDay(bestTime)
                        .isBestTime(true)
                        .rank(1)
                        .build();
                results.add(result);
            }
        }
        return results;
    }

    @Override
    public Integer getGlobalBestSendTime(Integer sendChannel) {
        if (!smartSendTimeConfig.getAllowGlobalBestTime()) {
            log.info("Global best time is disabled");
            return null;
        }

        List<Object[]> globalStats = userBehaviorStatsDao.findGlobalBestSendTime(sendChannel, 1);
        if (globalStats == null || globalStats.isEmpty()) {
            log.warn("No global stats found for channel:{}", sendChannel);
            return getDefaultSendTime();
        }

        Object[] topResult = globalStats.get(0);
        Integer hourOfDay = (Integer) topResult[0];

        // 应用时间窗口过滤
        if (smartSendTimeConfig.getTimeWindow().getEnabled()) {
            if (!isInTimeWindow(hourOfDay)) {
                return getDefaultSendTime();
            }
        }

        return hourOfDay;
    }

    @Override
    public Boolean isBestSendTime(String receiver, Integer sendChannel, Integer hourOfDay) {
        Integer bestTime = getBestSendTime(receiver, sendChannel);
        return bestTime != null && bestTime.equals(hourOfDay);
    }

    @Override
    public Long optimizeSendTime(String receiver, Integer sendChannel, Long originalTime) {
        if (!smartSendTimeConfig.getEnabled()) {
            return originalTime;
        }

        Integer bestHour = getBestSendTime(receiver, sendChannel);
        if (bestHour == null) {
            return originalTime;
        }

        // 将原始时间调整为最佳小时
        LocalDateTime originalDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(originalTime), ZoneId.systemDefault());

        LocalDateTime optimizedDateTime = originalDateTime
                .withHour(bestHour)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // 如果优化后的时间早于当前时间，则推迟到第二天
        if (optimizedDateTime.isBefore(LocalDateTime.now())) {
            optimizedDateTime = optimizedDateTime.plusDays(1);
        }

        return optimizedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取全局推荐发送时间列表
     */
    private List<SmartSendTimeResult> getGlobalRecommendedSendTimes(Integer sendChannel, Integer topN) {
        List<Object[]> globalStats = userBehaviorStatsDao.findGlobalBestSendTime(
                sendChannel, topN);

        List<SmartSendTimeResult> results = new ArrayList<>();
        for (int i = 0; i < globalStats.size(); i++) {
            Object[] stat = globalStats.get(i);
            SmartSendTimeResult result = SmartSendTimeResult.builder()
                    .hourOfDay((Integer) stat[0])
                    .sendChannel((Integer) stat[1])
                    .sampleSize(((Number) stat[2]).longValue())
                    .predictedOpenRate(((Number) stat[6]).doubleValue())
                    .predictedClickRate(((Number) stat[7]).doubleValue())
                    .predictedConversionRate(((Number) stat[8]).doubleValue())
                    .dataSource("GLOBAL")
                    .rank(i + 1)
                    .isBestTime(i == 0)
                    .build();

            // 计算综合评分
            result.setComprehensiveScore(calculateComprehensiveScore(result));
            // 计算置信度
            result.setConfidence(calculateConfidence(result.getSampleSize(), "GLOBAL"));

            results.add(result);
        }

        return results;
    }

    /**
     * 转换为结果对象列表
     */
    private List<SmartSendTimeResult> convertToResults(List<UserBehaviorStats> stats,
                                                      String receiver, Integer sendChannel,
                                                      String dataSource) {
        return stats.stream()
                .map(stat -> SmartSendTimeResult.builder()
                        .receiver(receiver)
                        .sendChannel(sendChannel)
                        .hourOfDay(stat.getHourOfDay())
                        .predictedOpenRate(stat.getOpenRate())
                        .predictedClickRate(stat.getClickRate())
                        .predictedConversionRate(stat.getConversionRate())
                        .sampleSize(stat.getSendCount())
                        .dataSource(dataSource)
                        .comprehensiveScore(calculateComprehensiveScore(stat))
                        .confidence(calculateConfidence(stat.getSendCount(), dataSource))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 根据策略排序
     */
    private List<SmartSendTimeResult> sortByStrategy(List<SmartSendTimeResult> results) {
        String strategy = smartSendTimeConfig.getStrategy();

        switch (strategy) {
            case "OPEN_RATE":
                results.sort(Comparator.comparing(SmartSendTimeResult::getPredictedOpenRate).reversed());
                break;
            case "CLICK_RATE":
                results.sort(Comparator.comparing(SmartSendTimeResult::getPredictedClickRate).reversed());
                break;
            case "CONVERSION_RATE":
                results.sort(Comparator.comparing(SmartSendTimeResult::getPredictedConversionRate).reversed());
                break;
            case "COMPREHENSIVE":
            default:
                results.sort(Comparator.comparing(SmartSendTimeResult::getComprehensiveScore).reversed());
                break;
        }

        return results;
    }

    /**
     * 时间窗口过滤
     */
    private List<SmartSendTimeResult> filterByTimeWindow(List<SmartSendTimeResult> results) {
        Integer earliestHour = smartSendTimeConfig.getTimeWindow().getEarliestHour();
        Integer latestHour = smartSendTimeConfig.getTimeWindow().getLatestHour();

        return results.stream()
                .filter(r -> r.getHourOfDay() >= earliestHour && r.getHourOfDay() <= latestHour)
                .collect(Collectors.toList());
    }

    /**
     * 检查是否在时间窗口内
     */
    private boolean isInTimeWindow(Integer hour) {
        Integer earliestHour = smartSendTimeConfig.getTimeWindow().getEarliestHour();
        Integer latestHour = smartSendTimeConfig.getTimeWindow().getLatestHour();
        return hour >= earliestHour && hour <= latestHour;
    }

    /**
     * 计算综合评分
     */
    private Double calculateComprehensiveScore(UserBehaviorStats stats) {
        SmartSendTimeConfig.WeightConfig weight = smartSendTimeConfig.getWeight();
        return stats.getOpenRate() * weight.getOpenRateWeight() +
               stats.getClickRate() * weight.getClickRateWeight() +
               stats.getConversionRate() * weight.getConversionRateWeight() +
               stats.getActivityScore() * weight.getActivityWeight();
    }

    /**
     * 计算综合评分（从结果对象）
     */
    private Double calculateComprehensiveScore(SmartSendTimeResult result) {
        SmartSendTimeConfig.WeightConfig weight = smartSendTimeConfig.getWeight();
        return result.getPredictedOpenRate() * weight.getOpenRateWeight() +
               result.getPredictedClickRate() * weight.getClickRateWeight() +
               result.getPredictedConversionRate() * weight.getConversionRateWeight();
    }

    /**
     * 计算置信度
     */
    private Double calculateConfidence(Long sampleSize, String dataSource) {
        if ("GLOBAL".equals(dataSource)) {
            if (sampleSize >= smartSendTimeConfig.getGlobalMinSampleSize()) {
                return 90.0;
            }
            return 60.0;
        }

        // 用户数据置信度计算
        if (sampleSize >= 100) {
            return 95.0;
        } else if (sampleSize >= 50) {
            return 85.0;
        } else if (sampleSize >= smartSendTimeConfig.getMinSampleSize()) {
            return 70.0;
        }
        return 50.0;
    }

    /**
     * 获取默认发送时间（上午10点）
     */
    private Integer getDefaultSendTime() {
        return 10;
    }
}
