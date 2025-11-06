package com.java3y.austin.support.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 线程池监控定时任务
 * 定期检查线程池状态并触发告警
 *
 * @author austin
 */
@Slf4j
@Component
public class ThreadPoolMonitorScheduler {

    private final ThreadPoolAlarmService alarmService;
    private final ThreadPoolAlarmRuleConfig alarmRuleConfig;

    public ThreadPoolMonitorScheduler(ThreadPoolAlarmService alarmService,
                                       ThreadPoolAlarmRuleConfig alarmRuleConfig) {
        this.alarmService = alarmService;
        this.alarmRuleConfig = alarmRuleConfig;
    }

    /**
     * 定时检查线程池状态并告警
     * 使用配置的检查间隔，默认30秒
     */
    @Scheduled(fixedDelayString = "${austin.thread-pool.alarm.check-interval:30}000")
    public void monitorThreadPools() {
        if (!alarmRuleConfig.getEnabled()) {
            return;
        }

        try {
            log.debug("开始执行线程池监控检查...");
            alarmService.checkAndAlarm();
            log.debug("线程池监控检查完成");
        } catch (Exception e) {
            log.error("ThreadPoolMonitorScheduler#monitorThreadPools error", e);
        }
    }
}
