package com.java3y.austin.support.monitor;

import cn.hutool.core.collection.CollUtil;
import com.java3y.austin.common.domain.MqBacklogAlertInfo;
import com.java3y.austin.common.enums.AlertLevel;
import com.java3y.austin.support.config.BacklogMonitorConfig;
import com.java3y.austin.support.dto.BacklogMonitorTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * MQ积压监控器
 * 定时采集各MQ的积压数据，判断是否触发告警
 *
 * @author austin
 */
@Slf4j
@Component
public class MqBacklogMonitor {

    @Autowired
    private BacklogMonitorConfig monitorConfig;

    @Autowired
    private MqBacklogAlertService alertService;

    @Autowired
    private List<MqBacklogAdapter> adapters;

    /**
     * MQ适配器映射表 (mqType -> adapter)
     */
    private Map<String, MqBacklogAdapter> adapterMap;

    /**
     * 告警冷却记录 (key: mqType:topic:consumerGroup:alertLevel -> lastAlertTime)
     */
    private final Map<String, Long> alertCooldownMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 初始化适配器映射表
        if (CollUtil.isNotEmpty(adapters)) {
            adapterMap = adapters.stream()
                    .collect(Collectors.toMap(
                            MqBacklogAdapter::getMqType,
                            adapter -> adapter,
                            (existing, replacement) -> existing
                    ));
            log.info("MqBacklogMonitor#init 初始化适配器完成，支持的MQ类型: {}", adapterMap.keySet());
        }
    }

    /**
     * 定时采集任务
     * 使用fixedDelayString从配置中读取间隔时间
     */
    @Scheduled(fixedDelayString = "${austin.monitor.backlog.collect.interval:30}000")
    public void collectAndMonitor() {
        // 1. 检查全局开关
        if (!Boolean.TRUE.equals(monitorConfig.getEnabled())) {
            return;
        }

        // 2. 获取监控目标配置列表
        List<BacklogMonitorTarget> targets = monitorConfig.getTargetList();
        if (CollUtil.isEmpty(targets)) {
            log.debug("MqBacklogMonitor#collectAndMonitor 未配置监控目标");
            return;
        }

        // 3. 遍历每个监控目标
        for (BacklogMonitorTarget target : targets) {
            if (!Boolean.TRUE.equals(target.getEnabled())) {
                continue;
            }

            try {
                monitorTarget(target);
            } catch (Exception e) {
                log.error("MqBacklogMonitor#collectAndMonitor 监控目标异常, target:{}", target, e);
            }
        }
    }

    /**
     * 监控单个目标
     *
     * @param target 监控目标
     */
    private void monitorTarget(BacklogMonitorTarget target) {
        // 1. 获取对应的MQ适配器
        MqBacklogAdapter adapter = adapterMap.get(target.getMqType());
        if (adapter == null) {
            log.warn("MqBacklogMonitor#monitorTarget 未找到对应的MQ适配器, mqType:{}", target.getMqType());
            return;
        }

        // 2. 检查MQ连接状态
        if (!adapter.checkConnection()) {
            log.error("MqBacklogMonitor#monitorTarget MQ连接异常, mqType:{}, topic:{}", 
                    target.getMqType(), target.getTopic());
            return;
        }

        // 3. 获取积压信息
        MqBacklogAlertInfo backlogInfo = adapter.getBacklogInfo(target.getTopic(), target.getConsumerGroup());
        if (backlogInfo == null) {
            log.warn("MqBacklogMonitor#monitorTarget 获取积压信息失败, target:{}", target);
            return;
        }

        // 4. 判断告警级别
        AlertLevel alertLevel = determineAlertLevel(backlogInfo.getCurrentBacklog(), target);
        if (alertLevel == null) {
            // 未超阈值，不需要告警
            log.debug("MqBacklogMonitor#monitorTarget 积压量正常, target:{}, backlog:{}", 
                    target, backlogInfo.getCurrentBacklog());
            return;
        }

        // 5. 检查冷却策略
        if (!shouldSendAlert(target, alertLevel)) {
            log.info("MqBacklogMonitor#monitorTarget 告警冷却中，跳过本次告警, target:{}, alertLevel:{}", 
                    target, alertLevel);
            return;
        }

        // 6. 完善告警信息并发送
        backlogInfo.setAlertLevel(alertLevel);
        backlogInfo.setThresholdBacklog(getThresholdByLevel(target, alertLevel));
        alertService.sendAlert(backlogInfo);

        // 7. 记录告警时间
        recordAlertTime(target, alertLevel);
    }

    /**
     * 判断告警级别
     *
     * @param currentBacklog 当前积压量
     * @param target         监控目标
     * @return 告警级别，null表示不需要告警
     */
    private AlertLevel determineAlertLevel(Long currentBacklog, BacklogMonitorTarget target) {
        // 优先使用目标配置的阈值，否则使用全局默认值
        Long criticalThreshold = target.getCriticalThreshold() != null 
                ? target.getCriticalThreshold() 
                : monitorConfig.getCriticalThreshold();
        Long warnThreshold = target.getWarnThreshold() != null 
                ? target.getWarnThreshold() 
                : monitorConfig.getDefaultThreshold();

        if (currentBacklog >= criticalThreshold) {
            return AlertLevel.CRITICAL;
        } else if (currentBacklog >= warnThreshold) {
            return AlertLevel.WARN;
        }

        return null;
    }

    /**
     * 获取指定级别的阈值
     *
     * @param target     监控目标
     * @param alertLevel 告警级别
     * @return 阈值
     */
    private Long getThresholdByLevel(BacklogMonitorTarget target, AlertLevel alertLevel) {
        if (AlertLevel.CRITICAL.equals(alertLevel)) {
            return target.getCriticalThreshold() != null 
                    ? target.getCriticalThreshold() 
                    : monitorConfig.getCriticalThreshold();
        } else if (AlertLevel.WARN.equals(alertLevel)) {
            return target.getWarnThreshold() != null 
                    ? target.getWarnThreshold() 
                    : monitorConfig.getDefaultThreshold();
        }
        return 0L;
    }

    /**
     * 判断是否应该发送告警（检查冷却期）
     *
     * @param target     监控目标
     * @param alertLevel 告警级别
     * @return true表示应该发送，false表示在冷却期内
     */
    private boolean shouldSendAlert(BacklogMonitorTarget target, AlertLevel alertLevel) {
        String key = buildCooldownKey(target, alertLevel);
        Long lastAlertTime = alertCooldownMap.get(key);

        if (lastAlertTime == null) {
            return true;
        }

        // 获取对应级别的冷却时间
        Integer cooldownSeconds = AlertLevel.CRITICAL.equals(alertLevel) 
                ? monitorConfig.getCooldownCritical() 
                : monitorConfig.getCooldownWarn();

        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - lastAlertTime) / 1000;

        return elapsedSeconds >= cooldownSeconds;
    }

    /**
     * 记录告警时间
     *
     * @param target     监控目标
     * @param alertLevel 告警级别
     */
    private void recordAlertTime(BacklogMonitorTarget target, AlertLevel alertLevel) {
        String key = buildCooldownKey(target, alertLevel);
        alertCooldownMap.put(key, System.currentTimeMillis());
    }

    /**
     * 构建冷却记录的key
     *
     * @param target     监控目标
     * @param alertLevel 告警级别
     * @return key
     */
    private String buildCooldownKey(BacklogMonitorTarget target, AlertLevel alertLevel) {
        return String.format("%s:%s:%s:%s", 
                target.getMqType(), 
                target.getTopic(), 
                target.getConsumerGroup(), 
                alertLevel);
    }
}
