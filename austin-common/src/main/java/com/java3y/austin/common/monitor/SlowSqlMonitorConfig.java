package com.java3y.austin.common.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 慢SQL监控配置类
 *
 * @author austin
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(SlowSqlProperties.class)
@ConditionalOnProperty(prefix = "austin.slow.sql", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SlowSqlMonitorConfig {

    @Autowired
    private SlowSqlProperties slowSqlProperties;

    /**
     * 包装数据源,添加慢SQL监控功能
     */
    @Bean
    public DataSource slowSqlDataSource(DataSource dataSource) {
        if (slowSqlProperties.getEnabled()) {
            log.info("【慢SQL监控】启用慢SQL监控, 阈值: {}ms", slowSqlProperties.getThreshold());
            return new SlowSqlDataSource(dataSource, slowSqlProperties.getThreshold());
        }
        log.info("【慢SQL监控】慢SQL监控未启用");
        return dataSource;
    }
}
