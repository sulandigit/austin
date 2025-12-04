package com.java3y.austin.common.monitor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 慢SQL监控配置属性
 *
 * @author austin
 */
@Data
@Component
@ConfigurationProperties(prefix = "austin.slow.sql")
public class SlowSqlProperties {

    /**
     * 是否启用慢SQL监控
     */
    private Boolean enabled = true;

    /**
     * 慢SQL阈值,单位毫秒
     * 默认1000ms,即1秒
     */
    private Long threshold = 1000L;

    /**
     * 是否打印SQL参数
     */
    private Boolean printParameters = true;

    /**
     * 是否启用告警
     */
    private Boolean alertEnabled = false;

    /**
     * 告警阈值,同一SQL在指定时间窗口内出现多少次慢查询时触发告警
     */
    private Integer alertThreshold = 10;

    /**
     * 告警时间窗口,单位分钟
     */
    private Integer alertTimeWindow = 5;
}
