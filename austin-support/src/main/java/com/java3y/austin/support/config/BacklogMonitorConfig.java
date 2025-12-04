package com.java3y.austin.support.config;

import com.alibaba.fastjson.JSON;
import com.java3y.austin.support.dto.BacklogMonitorTarget;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * MQ积压监控配置类
 *
 * @author austin
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "austin.monitor.backlog")
public class BacklogMonitorConfig {

    /**
     * 全局开关，默认关闭
     */
    private Boolean enabled = false;

    /**
     * 采集间隔（秒），默认30秒
     */
    private Integer collectInterval = 30;

    /**
     * 默认积压告警阈值，默认10000
     */
    private Long defaultThreshold = 10000L;

    /**
     * 严重积压告警阈值，默认50000
     */
    private Long criticalThreshold = 50000L;

    /**
     * WARN级别冷却时间（秒），默认10分钟
     */
    private Integer cooldownWarn = 600;

    /**
     * CRITICAL级别冷却时间（秒），默认5分钟
     */
    private Integer cooldownCritical = 300;

    /**
     * 监控目标配置列表（JSON格式）
     * 示例: [{"mqType":"redis","topic":"austinBusiness","consumerGroup":"default","enabled":true}]
     */
    private String targets = "[]";

    /**
     * 获取解析后的监控目标列表
     *
     * @return 监控目标列表
     */
    public List<BacklogMonitorTarget> getTargetList() {
        try {
            return JSON.parseArray(targets, BacklogMonitorTarget.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
