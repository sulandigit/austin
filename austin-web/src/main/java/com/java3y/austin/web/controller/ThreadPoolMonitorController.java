package com.java3y.austin.web.controller;

import com.java3y.austin.support.monitor.ThreadPoolMonitorCollector;
import com.java3y.austin.support.monitor.ThreadPoolMonitorMetrics;
import com.java3y.austin.web.vo.BasicResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 线程池监控控制器
 * 提供线程池监控数据查询接口
 *
 * @author austin
 */
@Slf4j
@RestController
@RequestMapping("/monitor/threadpool")
@Api(tags = "线程池监控")
public class ThreadPoolMonitorController {

    @Autowired
    private ThreadPoolMonitorCollector monitorCollector;

    /**
     * 查询所有线程池的监控指标
     *
     * @return 所有线程池的监控指标列表
     */
    @GetMapping("/metrics")
    @ApiOperation("获取所有线程池监控指标")
    public BasicResultVO<List<ThreadPoolMonitorMetrics>> getAllMetrics() {
        try {
            List<ThreadPoolMonitorMetrics> metricsList = monitorCollector.collectAllMetrics();
            return BasicResultVO.success(metricsList);
        } catch (Exception e) {
            log.error("ThreadPoolMonitorController#getAllMetrics error", e);
            return BasicResultVO.fail("查询线程池监控指标失败");
        }
    }

    /**
     * 查询指定线程池的监控指标
     *
     * @param poolName 线程池名称
     * @return 指定线程池的监控指标
     */
    @GetMapping("/metrics/{poolName}")
    @ApiOperation("获取指定线程池监控指标")
    public BasicResultVO<ThreadPoolMonitorMetrics> getMetricsByPoolName(@PathVariable String poolName) {
        try {
            ThreadPoolMonitorMetrics metrics = monitorCollector.collectMetrics(poolName);
            if (metrics == null) {
                return BasicResultVO.fail("线程池不存在: " + poolName);
            }
            return BasicResultVO.success(metrics);
        } catch (Exception e) {
            log.error("ThreadPoolMonitorController#getMetricsByPoolName error, poolName={}", poolName, e);
            return BasicResultVO.fail("查询线程池监控指标失败");
        }
    }
}
