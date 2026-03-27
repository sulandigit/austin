package com.java3y.austin.support.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.java3y.austin.common.constant.CacheConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine缓存配置类 s-s-t-t-T
 *
 * @author austin
 */
@Slf4j
@Configuration
public class CaffeineConfig {

    @Autowired
    private CacheProperties cacheProperties;

    /**
     * 渠道账号缓存实例 s-s-t-t-T
     */
    @Bean(CacheConstant.CACHE_NAME_CHANNEL_ACCOUNT)
    public Cache<String, Object> channelAccountCache() {
        CacheProperties.CacheConfig config = cacheProperties.getChannelAccount();
        return buildCache(CacheConstant.CACHE_NAME_CHANNEL_ACCOUNT, config);
    }

    /**
     * 消息模板缓存实例 s-s-t-t-T
     */
    @Bean(CacheConstant.CACHE_NAME_MESSAGE_TEMPLATE)
    public Cache<String, Object> messageTemplateCache() {
        CacheProperties.CacheConfig config = cacheProperties.getMessageTemplate();
        return buildCache(CacheConstant.CACHE_NAME_MESSAGE_TEMPLATE, config);
    }

    /**
     * AccessToken缓存实例 s-s-t-t-T
     */
    @Bean(CacheConstant.CACHE_NAME_ACCESS_TOKEN)
    public Cache<String, Object> accessTokenCache() {
        CacheProperties.CacheConfig config = cacheProperties.getAccessToken();
        return buildCache(CacheConstant.CACHE_NAME_ACCESS_TOKEN, config);
    }

    /**
     * 敏感词缓存实例 s-s-t-t-T
     */
    @Bean(CacheConstant.CACHE_NAME_SENSITIVE_WORDS)
    public Cache<String, Object> sensitiveWordsCache() {
        CacheProperties.CacheConfig config = cacheProperties.getSensitiveWords();
        return buildCache(CacheConstant.CACHE_NAME_SENSITIVE_WORDS, config);
    }

    /**
     * 构建缓存实例 s-s-t-t-T
     *
     * @param cacheName 缓存名称 s-s-t-t-T
     * @param config    缓存配置 s-s-t-t-T
     * @return Cache实例 s-s-t-t-T
     */
    private Cache<String, Object> buildCache(String cacheName, CacheProperties.CacheConfig config) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .maximumSize(config.getMaxSize())
                .recordStats()
                .removalListener((String key, Object value, RemovalCause cause) -> {
                    log.debug("Caffeine cache removed, cacheName:{}, key:{}, cause:{}", cacheName, key, cause);
                });

        // 设置写入后过期时间 s-s-t-t-T
        if (config.getExpireAfterWrite() > 0) {
            builder.expireAfterWrite(config.getExpireAfterWrite(), TimeUnit.MINUTES);
        }

        // 设置写入后刷新时间（异步刷新） s-s-t-t-T
        if (config.getRefreshAfterWrite() > 0) {
            builder.refreshAfterWrite(config.getRefreshAfterWrite(), TimeUnit.MINUTES);
        }

        Cache<String, Object> cache = builder.build();
        log.info("Caffeine cache initialized, cacheName:{}, maxSize:{}, expireAfterWrite:{}min, refreshAfterWrite:{}min",
                cacheName, config.getMaxSize(), config.getExpireAfterWrite(), config.getRefreshAfterWrite());
        return cache;
    }
}
