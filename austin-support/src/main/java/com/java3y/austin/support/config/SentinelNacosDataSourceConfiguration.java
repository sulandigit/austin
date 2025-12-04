package com.java3y.austin.support.config;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.java3y.austin.support.constans.SentinelConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Sentinel Nacos 规则数据源配置
 * <p>
 * 功能：
 * 1. 从 Nacos 配置中心动态拉取 Sentinel 规则
 * 2. 监听 Nacos 配置变更，实时刷新规则
 * 3. 支持流控、熔断、系统保护等多种规则类型
 *
 * @author austin
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "austin.sentinel.enabled", havingValue = "true", matchIfMissing = false)
public class SentinelNacosDataSourceConfiguration {

    @Value("${austin.sentinel.nacos.server-addr:}")
    private String nacosServerAddr;

    @Value("${austin.sentinel.nacos.namespace:}")
    private String nacosNamespace;

    @Value("${austin.sentinel.nacos.group-id:DEFAULT_GROUP}")
    private String nacosGroupId;

    @Value("${spring.application.name:austin}")
    private String applicationName;

    /**
     * 初始化 Nacos 数据源
     */
    @PostConstruct
    public void init() {
        if (nacosServerAddr == null || nacosServerAddr.trim().isEmpty()) {
            log.warn("[SentinelNacosDataSource] Nacos server address is not configured, skip Sentinel Nacos data source initialization");
            return;
        }

        try {
            // 初始化流控规则数据源
            initFlowRuleDataSource();

            // 初始化熔断规则数据源
            initDegradeRuleDataSource();

            // 初始化系统保护规则数据源
            initSystemRuleDataSource();

            log.info("[SentinelNacosDataSource] Sentinel Nacos data source initialized successfully, server: {}, namespace: {}", 
                    nacosServerAddr, nacosNamespace);
        } catch (Exception e) {
            log.error("[SentinelNacosDataSource] Failed to initialize Sentinel Nacos data source", e);
        }
    }

    /**
     * 初始化流控规则数据源
     */
    private void initFlowRuleDataSource() {
        String dataId = applicationName + SentinelConstant.NACOS_DATA_ID_POSTFIX_FLOW;
        
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(
                nacosServerAddr,
                nacosGroupId,
                dataId,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {})
        );

        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
        log.info("[SentinelNacosDataSource] Flow rule data source registered, dataId: {}", dataId);
    }

    /**
     * 初始化熔断规则数据源
     */
    private void initDegradeRuleDataSource() {
        String dataId = applicationName + SentinelConstant.NACOS_DATA_ID_POSTFIX_DEGRADE;
        
        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new NacosDataSource<>(
                nacosServerAddr,
                nacosGroupId,
                dataId,
                source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {})
        );

        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
        log.info("[SentinelNacosDataSource] Degrade rule data source registered, dataId: {}", dataId);
    }

    /**
     * 初始化系统保护规则数据源
     */
    private void initSystemRuleDataSource() {
        String dataId = applicationName + SentinelConstant.NACOS_DATA_ID_POSTFIX_SYSTEM;
        
        ReadableDataSource<String, List<SystemRule>> systemRuleDataSource = new NacosDataSource<>(
                nacosServerAddr,
                nacosGroupId,
                dataId,
                source -> JSON.parseObject(source, new TypeReference<List<SystemRule>>() {})
        );

        SystemRuleManager.register2Property(systemRuleDataSource.getProperty());
        log.info("[SentinelNacosDataSource] System rule data source registered, dataId: {}", dataId);
    }
}
