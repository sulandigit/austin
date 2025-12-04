package com.java3y.austin.support.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 核心配置类
 * <p>
 * 功能：
 * 1. 启用 Sentinel 注解支持
 * 2. 配置全局的 Sentinel 初始化参数
 * 3. 根据配置开关控制 Sentinel 是否启用
 *
 * @author austin
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "austin.sentinel.enabled", havingValue = "true", matchIfMissing = false)
public class SentinelConfiguration {

    /**
     * 注册 Sentinel 注解切面支持
     * 用于 @SentinelResource 注解的资源定义和保护
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        log.info("[SentinelConfiguration] Sentinel annotation aspect enabled");
        return new SentinelResourceAspect();
    }
}
