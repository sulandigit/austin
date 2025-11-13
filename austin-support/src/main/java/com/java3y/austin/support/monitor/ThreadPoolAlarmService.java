package com.java3y.austin.support.monitor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程池告警服务
 *
 * @author 3y
 */
@Slf4j
@Service
public class ThreadPoolAlarmService {

    @Autowired
    private ThreadPoolAlarmConfig alarmConfig;

    /**
     * 记录每个线程池的最后告警时间，防止频繁告警
     */
    private final Map<String, Long> lastAlarmTimeMap = new ConcurrentHashMap<>();

    /**
     * 检查线程池指标并触发告警
     *
     * @param metricsList 线程池指标列表
     */
    public void checkAndAlarm(List<ThreadPoolMetrics> metricsList) {
        if (!alarmConfig.getEnabled()) {
            log.debug("Thread pool alarm is disabled");
            return;
        }

        if (CollectionUtil.isEmpty(metricsList)) {
            return;
        }

        for (ThreadPoolMetrics metrics : metricsList) {
            checkSinglePoolAndAlarm(metrics);
        }
    }

    /**
     * 检查单个线程池并触发告警
     *
     * @param metrics 线程池指标
     */
    private void checkSinglePoolAndAlarm(ThreadPoolMetrics metrics) {
        String poolName = metrics.getPoolName();

        // 检查是否在告警间隔内
        if (!shouldAlarm(poolName)) {
            return;
        }

        StringBuilder alarmMessage = new StringBuilder();
        boolean needAlarm = false;

        // 检查队列使用率
        if (metrics.getQueueUsageRate() >= alarmConfig.getQueueUsageThreshold()) {
            alarmMessage.append(String.format("[队列使用率告警] 当前使用率: %.2f%%, 阈值: %.2f%%, 队列大小: %d/%d; ",
                    metrics.getQueueUsageRate(), alarmConfig.getQueueUsageThreshold(),
                    metrics.getQueueSize(), metrics.getQueueCapacity()));
            needAlarm = true;
        }

        // 检查线程池使用率
        if (metrics.getPoolUsageRate() >= alarmConfig.getPoolUsageThreshold()) {
            alarmMessage.append(String.format("[线程池使用率告警] 当前使用率: %.2f%%, 阈值: %.2f%%, 活跃线程: %d/%d; ",
                    metrics.getPoolUsageRate(), alarmConfig.getPoolUsageThreshold(),
                    metrics.getActiveCount(), metrics.getMaximumPoolSize()));
            needAlarm = true;
        }

        // 检查是否触发拒绝策略
        if (alarmConfig.getRejectAlarmEnabled() && metrics.getRejected()) {
            alarmMessage.append("[拒绝策略告警] 线程池已满，新任务可能被拒绝; ");
            needAlarm = true;
        }

        if (needAlarm) {
            sendAlarm(poolName, metrics, alarmMessage.toString());
            updateLastAlarmTime(poolName);
        }
    }

    /**
     * 判断是否应该告警（基于告警间隔）
     *
     * @param poolName 线程池名称
     * @return 是否应该告警
     */
    private boolean shouldAlarm(String poolName) {
        Long lastAlarmTime = lastAlarmTimeMap.get(poolName);
        if (lastAlarmTime == null) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long interval = (currentTime - lastAlarmTime) / 1000; // 转换为秒
        return interval >= alarmConfig.getAlarmInterval();
    }

    /**
     * 更新最后告警时间
     *
     * @param poolName 线程池名称
     */
    private void updateLastAlarmTime(String poolName) {
        lastAlarmTimeMap.put(poolName, System.currentTimeMillis());
    }

    /**
     * 发送告警
     *
     * @param poolName 线程池名称
     * @param metrics 线程池指标
     * @param alarmMessage 告警消息
     */
    private void sendAlarm(String poolName, ThreadPoolMetrics metrics, String alarmMessage) {
        // 构建完整的告警信息
        String fullMessage = String.format(
                "【线程池告警】\n" +
                "线程池名称: %s\n" +
                "告警内容: %s\n" +
                "详细信息:\n" +
                "- 核心线程数: %d\n" +
                "- 最大线程数: %d\n" +
                "- 当前线程数: %d\n" +
                "- 活跃线程数: %d\n" +
                "- 队列容量: %d\n" +
                "- 队列大小: %d\n" +
                "- 队列剩余: %d\n" +
                "- 已完成任务: %d\n" +
                "- 总任务数: %d\n" +
                "- 采集时间: %s",
                poolName,
                alarmMessage,
                metrics.getCorePoolSize(),
                metrics.getMaximumPoolSize(),
                metrics.getPoolSize(),
                metrics.getActiveCount(),
                metrics.getQueueCapacity(),
                metrics.getQueueSize(),
                metrics.getQueueRemainingCapacity(),
                metrics.getCompletedTaskCount(),
                metrics.getTaskCount(),
                DateUtil.formatDateTime(metrics.getCollectTime())
        );

        // 记录告警日志
        log.warn(fullMessage);

        // TODO: 这里可以扩展其他告警方式
        // 1. 发送邮件告警
        // 2. 发送短信告警
        // 3. 发送钉钉/企业微信告警
        // 4. 调用监控平台接口
    }

    /**
     * 获取指定线程池的最后告警时间
     *
     * @param poolName 线程池名称
     * @return 最后告警时间
     */
    public Date getLastAlarmTime(String poolName) {
        Long lastTime = lastAlarmTimeMap.get(poolName);
        return lastTime != null ? new Date(lastTime) : null;
    }

    /**
     * 清除所有告警记录
     */
    public void clearAlarmHistory() {
        lastAlarmTimeMap.clear();
        log.info("Thread pool alarm history cleared");
    }
}
