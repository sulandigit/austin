package com.java3y.austin.support.config;

import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 系统保护规则配置
 * <p>
 * 功能：
 * 1. 配置系统级别的保护规则（CPU、Load、RT、线程数等）
 * 2. 当系统负载过高时，自动触发限流保护
 * 3. 优先使用 Nacos 动态配置，这里提供默认兜底规则
 *
 * @author austin
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "austin.sentinel.enabled", havingValue = "true", matchIfMissing = false)
public class SentinelSystemRuleConfiguration {

    @Value("${austin.sentinel.system.max-cpu-usage:0.8}")
    private double maxCpuUsage;

    @Value("${austin.sentinel.system.max-load:8.0}")
    private double maxLoad;

    @Value("${austin.sentinel.system.max-rt:3000}")
    private long maxRt;

    @Value("${austin.sentinel.system.max-thread:100}")
    private long maxThread;

    @Value("${austin.sentinel.system.max-qps:-1}")
    private double maxQps;

    /**
     * 初始化系统保护规则
     * 注意：这里的规则仅作为兜底，建议通过 Nacos 动态配置
     */
    @PostConstruct
    public void initSystemRules() {
        List<SystemRule> rules = new ArrayList<>();

        // CPU 使用率保护
        if (maxCpuUsage > 0 && maxCpuUsage <= 1) {
            SystemRule cpuRule = new SystemRule();
            cpuRule.setHighestCpuUsage(maxCpuUsage);
            rules.add(cpuRule);
            log.info("[SentinelSystemRule] CPU usage rule initialized: maxCpuUsage={}", maxCpuUsage);
        }

        // 系统 Load 保护（仅 Linux 生效）
        if (maxLoad > 0) {
            SystemRule loadRule = new SystemRule();
            loadRule.setHighestSystemLoad(maxLoad);
            rules.add(loadRule);
            log.info("[SentinelSystemRule] System load rule initialized: maxLoad={}", maxLoad);
        }

        // 平均响应时间保护
        if (maxRt > 0) {
            SystemRule rtRule = new SystemRule();
            rtRule.setAvgRt(maxRt);
            rules.add(rtRule);
            log.info("[SentinelSystemRule] Average RT rule initialized: maxRt={}ms", maxRt);
        }

        // 并发线程数保护
        if (maxThread > 0) {
            SystemRule threadRule = new SystemRule();
            threadRule.setMaxThread(maxThread);
            rules.add(threadRule);
            log.info("[SentinelSystemRule] Max thread rule initialized: maxThread={}", maxThread);
        }

        // QPS 保护
        if (maxQps > 0) {
            SystemRule qpsRule = new SystemRule();
            qpsRule.setQps(maxQps);
            rules.add(qpsRule);
            log.info("[SentinelSystemRule] QPS rule initialized: maxQps={}", maxQps);
        }

        // 加载规则
        if (!rules.isEmpty()) {
            SystemRuleManager.loadRules(rules);
            log.info("[SentinelSystemRule] {} system protection rules loaded", rules.size());
        } else {
            log.warn("[SentinelSystemRule] No system protection rules configured, using Nacos configuration");
        }
    }
}
