package com.java3y.austin.web.controller;

import com.java3y.austin.support.monitor.ThreadPoolAlarmService;
import com.java3y.austin.support.monitor.ThreadPoolMetrics;
import com.java3y.austin.support.monitor.ThreadPoolMetricsCollector;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 线程池监控控制器
 *
 * @author 3y
 */
@Slf4j
@RestController
@RequestMapping("/threadPool")
@Api(tags = "线程池监控")
public class ThreadPoolMonitorController {

    @Autowired
    private ThreadPoolMetricsCollector metricsCollector;

    @Autowired
    private ThreadPoolAlarmService alarmService;

    /**
     * 获取所有线程池监控指标
     *
     * @return 线程池指标列表
     */
    @GetMapping("/metrics")
    @ApiOperation("获取所有线程池监控指标")
    public List<ThreadPoolMetrics> getAllMetrics() {
        return metricsCollector.collectAllMetrics();
    }

    /**
     * 获取指定线程池监控指标
     *
     * @param poolName 线程池名称
     * @return 线程池指标
     */
    @GetMapping("/metrics/{poolName}")
    @ApiOperation("获取指定线程池监控指标")
    public ThreadPoolMetrics getMetricsByName(@PathVariable String poolName) {
        return metricsCollector.collectMetricsByName(poolName);
    }

    /**
     * 获取线程池告警历史
     *
     * @param poolName 线程池名称
     * @return 告警信息
     */
    @GetMapping("/alarm/history/{poolName}")
    @ApiOperation("获取线程池最后告警时间")
    public Map<String, Object> getAlarmHistory(@PathVariable String poolName) {
        Map<String, Object> result = new HashMap<>();
        Date lastAlarmTime = alarmService.getLastAlarmTime(poolName);
        result.put("poolName", poolName);
        result.put("lastAlarmTime", lastAlarmTime);
        result.put("hasAlarm", lastAlarmTime != null);
        return result;
    }

    /**
     * 清除告警历史
     *
     * @return 操作结果
     */
    @PostMapping("/alarm/clear")
    @ApiOperation("清除所有告警历史")
    public Map<String, String> clearAlarmHistory() {
        alarmService.clearAlarmHistory();
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "告警历史已清除");
        return result;
    }
}
