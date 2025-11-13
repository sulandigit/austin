package com.java3y.austin.support.monitor;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 线程池监控定时任务
 *
 * @author 3y
 */
@Slf4j
@Component
public class ThreadPoolMonitorTask {

    @Autowired
    private ThreadPoolMetricsCollector metricsCollector;

    @Autowired
    private ThreadPoolAlarmService alarmService;

    @Autowired
    private ThreadPoolAlarmConfig alarmConfig;

    /**
     * 定时监控线程池状态
     * 使用配置的监控间隔，默认30秒执行一次
     */
    @Scheduled(fixedDelayString = "${austin.thread-pool.alarm.monitor-interval:30}000")
    public void monitorThreadPools() {
        try {
            if (!alarmConfig.getEnabled()) {
                return;
            }

            // 收集所有线程池的监控指标
            List<ThreadPoolMetrics> metricsList = metricsCollector.collectAllMetrics();

            if (CollectionUtil.isEmpty(metricsList)) {
                log.debug("No thread pool metrics collected");
                return;
            }

            // 打印监控日志
            for (ThreadPoolMetrics metrics : metricsList) {
                log.info("Thread pool monitor - poolName: {}, activeCount: {}/{}, queueSize: {}/{}, " +
                        "queueUsage: {:.2f}%, poolUsage: {:.2f}%, completedTasks: {}",
                        metrics.getPoolName(),
                        metrics.getActiveCount(),
                        metrics.getMaximumPoolSize(),
                        metrics.getQueueSize(),
                        metrics.getQueueCapacity(),
                        metrics.getQueueUsageRate(),
                        metrics.getPoolUsageRate(),
                        metrics.getCompletedTaskCount());
            }

            // 检查并触发告警
            alarmService.checkAndAlarm(metricsList);

        } catch (Exception e) {
            log.error("ThreadPoolMonitorTask.monitorThreadPools error", e);
        }
    }

    /**
     * 定时清理告警历史记录（每天凌晨3点执行）
     * 防止告警历史记录无限增长
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void clearAlarmHistory() {
        try {
            alarmService.clearAlarmHistory();
        } catch (Exception e) {
            log.error("ThreadPoolMonitorTask.clearAlarmHistory error", e);
        }
    }
}
