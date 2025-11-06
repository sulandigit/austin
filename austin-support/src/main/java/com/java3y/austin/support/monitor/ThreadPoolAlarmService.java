package com.java3y.austin.support.monitor;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程池告警服务
 * 负责检测线程池异常情况并发送告警通知
 *
 * @author austin
 */
@Slf4j
@Service
public class ThreadPoolAlarmService {

    private final ThreadPoolMonitorCollector monitorCollector;
    private final ThreadPoolAlarmRuleConfig alarmRuleConfig;
    private final ThreadPoolAlarmNotifier alarmNotifier;

    /**
     * 记录上次告警时间，用于实现告警冷却
     * key: threadPoolName + alarmType
     */
    private final Map<String, Long> lastAlarmTimeMap = new ConcurrentHashMap<>();

    public ThreadPoolAlarmService(ThreadPoolMonitorCollector monitorCollector,
                                   ThreadPoolAlarmRuleConfig alarmRuleConfig,
                                   ThreadPoolAlarmNotifier alarmNotifier) {
        this.monitorCollector = monitorCollector;
        this.alarmRuleConfig = alarmRuleConfig;
        this.alarmNotifier = alarmNotifier;
    }

    /**
     * 检查所有线程池并发送告警
     */
    public void checkAndAlarm() {
        if (!alarmRuleConfig.getEnabled()) {
            return;
        }

        try {
            // 收集所有线程池的监控指标
            var metricsList = monitorCollector.collectAllMetrics();
            
            for (ThreadPoolMonitorMetrics metrics : metricsList) {
                String poolName = metrics.getThreadPoolName();
                
                // 检查该线程池是否启用告警
                if (!alarmRuleConfig.isPoolAlarmEnabled(poolName)) {
                    continue;
                }
                
                // 检查队列使用率
                checkQueueUsage(metrics);
                
                // 检查线程池活跃度
                checkLiveness(metrics);
                
                // 检查拒绝任务数
                checkRejectCount(metrics);
            }
        } catch (Exception e) {
            log.error("ThreadPoolAlarmService#checkAndAlarm error", e);
        }
    }

    /**
     * 检查队列使用率
     */
    private void checkQueueUsage(ThreadPoolMonitorMetrics metrics) {
        String poolName = metrics.getThreadPoolName();
        Integer threshold = alarmRuleConfig.getQueueUsageThreshold(poolName);
        Double queueUsageRate = metrics.getQueueUsageRate();
        
        if (queueUsageRate >= threshold) {
            ThreadPoolAlarmEvent event = ThreadPoolAlarmEvent.builder()
                    .alarmType(ThreadPoolAlarmEvent.AlarmType.QUEUE_USAGE_HIGH)
                    .threadPoolName(poolName)
                    .title("线程池队列使用率告警")
                    .message(String.format("线程池[%s]队列使用率过高！", poolName))
                    .currentValue(queueUsageRate + "%")
                    .threshold(threshold + "%")
                    .metrics(metrics)
                    .timestamp(System.currentTimeMillis())
                    .level(getAlarmLevel(queueUsageRate, threshold))
                    .build();
            
            sendAlarmIfNeeded(event);
        }
    }

    /**
     * 检查线程池活跃度
     */
    private void checkLiveness(ThreadPoolMonitorMetrics metrics) {
        String poolName = metrics.getThreadPoolName();
        Integer threshold = alarmRuleConfig.getLivenessThreshold(poolName);
        Double liveness = metrics.getLiveness();
        
        if (liveness >= threshold) {
            ThreadPoolAlarmEvent event = ThreadPoolAlarmEvent.builder()
                    .alarmType(ThreadPoolAlarmEvent.AlarmType.LIVENESS_HIGH)
                    .threadPoolName(poolName)
                    .title("线程池活跃度告警")
                    .message(String.format("线程池[%s]活跃度过高！", poolName))
                    .currentValue(liveness + "%")
                    .threshold(threshold + "%")
                    .metrics(metrics)
                    .timestamp(System.currentTimeMillis())
                    .level(getAlarmLevel(liveness, threshold))
                    .build();
            
            sendAlarmIfNeeded(event);
        }
    }

    /**
     * 检查拒绝任务数
     */
    private void checkRejectCount(ThreadPoolMonitorMetrics metrics) {
        String poolName = metrics.getThreadPoolName();
        Long threshold = alarmRuleConfig.getRejectCountThreshold(poolName);
        Long rejectCount = metrics.getRejectCount();
        
        if (rejectCount >= threshold) {
            ThreadPoolAlarmEvent event = ThreadPoolAlarmEvent.builder()
                    .alarmType(ThreadPoolAlarmEvent.AlarmType.REJECT_COUNT_HIGH)
                    .threadPoolName(poolName)
                    .title("线程池拒绝任务告警")
                    .message(String.format("线程池[%s]拒绝任务数过多！", poolName))
                    .currentValue(String.valueOf(rejectCount))
                    .threshold(String.valueOf(threshold))
                    .metrics(metrics)
                    .timestamp(System.currentTimeMillis())
                    .level(ThreadPoolAlarmEvent.AlarmLevel.CRITICAL)
                    .build();
            
            sendAlarmIfNeeded(event);
        }
    }

    /**
     * 根据当前值和阈值判断告警级别
     */
    private ThreadPoolAlarmEvent.AlarmLevel getAlarmLevel(Double currentValue, Integer threshold) {
        if (currentValue >= threshold * 1.2) {
            return ThreadPoolAlarmEvent.AlarmLevel.URGENT;
        } else if (currentValue >= threshold * 1.1) {
            return ThreadPoolAlarmEvent.AlarmLevel.CRITICAL;
        } else {
            return ThreadPoolAlarmEvent.AlarmLevel.WARNING;
        }
    }

    /**
     * 如果满足条件则发送告警（考虑冷却时间）
     */
    private void sendAlarmIfNeeded(ThreadPoolAlarmEvent event) {
        String key = event.getThreadPoolName() + ":" + event.getAlarmType();
        Long lastAlarmTime = lastAlarmTimeMap.get(key);
        long currentTime = System.currentTimeMillis();
        
        // 检查是否在冷却期内
        if (lastAlarmTime != null) {
            long timeSinceLastAlarm = (currentTime - lastAlarmTime) / 1000;
            if (timeSinceLastAlarm < alarmRuleConfig.getCooldownPeriod()) {
                log.debug("告警在冷却期内，跳过发送: {}", key);
                return;
            }
        }
        
        // 发送告警
        try {
            alarmNotifier.sendAlarm(event);
            lastAlarmTimeMap.put(key, currentTime);
            log.info("发送线程池告警: poolName={}, type={}, currentValue={}, threshold={}", 
                    event.getThreadPoolName(), event.getAlarmType(), 
                    event.getCurrentValue(), event.getThreshold());
        } catch (Exception e) {
            log.error("发送线程池告警失败: {}", JSON.toJSONString(event), e);
        }
    }
}
