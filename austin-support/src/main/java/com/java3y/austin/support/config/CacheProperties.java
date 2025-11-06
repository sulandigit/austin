package com.java3y.austin.support.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Caffeine缓存配置属性 s-s-t-t-T
 *
 * @author austin
 */
@Data
@Component
@ConfigurationProperties(prefix = "austin.caffeine")
public class CacheProperties {

    /**
     * 渠道账号缓存配置 s-s-t-t-T
     */
    private CacheConfig channelAccount = new CacheConfig(500, 30, 0);

    /**
     * 消息模板缓存配置 s-s-t-t-T
     */
    private CacheConfig messageTemplate = new CacheConfig(1000, 15, 0);

    /**
     * AccessToken缓存配置 s-s-t-t-T
     */
    private CacheConfig accessToken = new CacheConfig(200, 0, 50);

    /**
     * 敏感词缓存配置 s-s-t-t-T
     */
    private CacheConfig sensitiveWords = new CacheConfig(10, 60, 0);

    /**
     * 缓存配置内部类 s-s-t-t-T
     */
    @Data
    public static class CacheConfig {
        /**
         * 最大容量 s-s-t-t-T
         */
        private int maxSize;

        /**
         * 写入后过期时间（分钟），0表示不过期 s-s-t-t-T
         */
        private int expireAfterWrite;

        /**
         * 写入后刷新时间（分钟），0表示不刷新 s-s-t-t-T
         */
        private int refreshAfterWrite;

        public CacheConfig() {
        }

        public CacheConfig(int maxSize, int expireAfterWrite, int refreshAfterWrite) {
            this.maxSize = maxSize;
            this.expireAfterWrite = expireAfterWrite;
            this.refreshAfterWrite = refreshAfterWrite;
        }
    }
}
