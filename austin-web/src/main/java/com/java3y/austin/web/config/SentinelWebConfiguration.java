package com.java3y.austin.web.config;

import com.alibaba.csp.sentinel.adapter.servlet.CommonFilter;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.java3y.austin.support.constans.SentinelConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

/**
 * Sentinel Web 层配置
 * <p>
 * 功能：
 * 1. 注册 Sentinel Web 过滤器，拦截所有 HTTP 请求
 * 2. 自动为每个 HTTP 接口创建 Sentinel 资源
 * 3. 配置资源命名规则和 Block 异常处理器
 *
 * @author austin
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "austin.sentinel.enabled", havingValue = "true", matchIfMissing = false)
public class SentinelWebConfiguration {

    /**
     * 注册 Sentinel Web 过滤器
     */
    @Bean
    public FilterRegistrationBean<Filter> sentinelFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CommonFilter());
        registration.addUrlPatterns("/*");
        registration.setName("sentinelFilter");
        registration.setOrder(1);

        log.info("[SentinelWebConfiguration] Sentinel web filter registered");
        return registration;
    }

    /**
     * 配置 URL 资源清理规则
     * 将 URL 参数清理，避免资源名过多
     */
    @Bean
    public SentinelWebConfiguration.WebCallbackConfig webCallbackConfig() {
        // 配置 URL 清理规则，去除 URL 参数
        WebCallbackManager.setUrlCleaner(url -> {
            if (url == null) {
                return null;
            }
            // 去除 URL 参数
            int index = url.indexOf('?');
            if (index > 0) {
                return url.substring(0, index);
            }
            return url;
        });

        // 配置资源命名规则：添加统一前缀
        WebCallbackManager.setRequestOriginParser(request -> {
            String origin = request.getHeader("origin");
            return origin != null ? origin : "unknown";
        });

        log.info("[SentinelWebConfiguration] Sentinel web callback configured");
        return new WebCallbackConfig();
    }

    /**
     * 内部配置类，用于标记配置完成
     */
    private static class WebCallbackConfig {
    }
}
