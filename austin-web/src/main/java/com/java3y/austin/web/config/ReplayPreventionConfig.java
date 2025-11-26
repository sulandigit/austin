package com.java3y.austin.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 防重放配置类
 *
 * @author austin
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "austin.replay.prevention")
public class ReplayPreventionConfig {

    /**
     * 是否启用防重放机制
     */
    private boolean enabled = true;

    /**
     * 默认时间窗口(秒)
     */
    private long defaultTimeWindow = 300;

    /**
     * 签名密钥
     */
    private String signSecret = "austin-anti-replay-secret-key";

    /**
     * Redis键前缀
     */
    private String redisKeyPrefix = "austin:anti-replay:";

    /**
     * 签名算法 (支持: MD5, SHA256, HMAC-SHA256)
     */
    private String signAlgorithm = "HMAC-SHA256";

    /**
     * 是否记录防重放日志
     */
    private boolean logEnabled = true;
}
