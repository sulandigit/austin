package com.java3y.austin.support.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存管理器
 * 使用 Caffeine 实现高性能本地缓存，作为 Redis 的 L1 缓存
 *
 * @author 3y
 */
@Slf4j
@Component
public class LocalCacheManager {

    /**
     * 缓存容器Map，key为缓存名称，value为具体的缓存对象
     */
    private final Map<String, Cache<String, Object>> cacheMap = new ConcurrentHashMap<>();

    /**
     * 默认缓存配置
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 100;
    private static final int DEFAULT_MAXIMUM_SIZE = 10000;
    private static final long DEFAULT_EXPIRE_AFTER_WRITE = 5L;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;

    /**
     * 缓存名称常量
     */
    public static final String CACHE_NAME_ACCOUNT = "account";
    public static final String CACHE_NAME_TEMPLATE = "template";
    public static final String CACHE_NAME_API_KEY = "apikey";

    @PostConstruct
    public void init() {
        log.info("LocalCacheManager init start...");
        
        // 初始化账号缓存（5分钟过期）
        createCache(CACHE_NAME_ACCOUNT, DEFAULT_MAXIMUM_SIZE, 5L, TimeUnit.MINUTES);
        
        // 初始化模板缓存（15分钟过期）
        createCache(CACHE_NAME_TEMPLATE, 5000, 15L, TimeUnit.MINUTES);
        
        // 初始化API Key缓存（60分钟过期）
        createCache(CACHE_NAME_API_KEY, DEFAULT_MAXIMUM_SIZE, 60L, TimeUnit.MINUTES);
        
        log.info("LocalCacheManager init success! cache count:{}", cacheMap.size());
    }

    /**
     * 创建缓存
     *
     * @param cacheName          缓存名称
     * @param maximumSize        最大容量
     * @param expireAfterWrite   写入后多久过期
     * @param timeUnit           时间单位
     */
    public void createCache(String cacheName, int maximumSize, long expireAfterWrite, TimeUnit timeUnit) {
        Cache<String, Object> cache = Caffeine.newBuilder()
                .initialCapacity(DEFAULT_INITIAL_CAPACITY)
                .maximumSize(maximumSize)
                .expireAfterWrite(expireAfterWrite, timeUnit)
                .recordStats()
                .build();
        
        cacheMap.put(cacheName, cache);
        log.info("LocalCacheManager create cache success! cacheName:{}, maximumSize:{}, expireAfterWrite:{}{}", 
                cacheName, maximumSize, expireAfterWrite, timeUnit);
    }

    /**
     * 获取缓存
     *
     * @param cacheName 缓存名称
     * @param key       缓存key
     * @return 缓存值
     */
    public Object get(String cacheName, String key) {
        try {
            Cache<String, Object> cache = cacheMap.get(cacheName);
            if (cache == null) {
                log.warn("LocalCacheManager#get cache not found! cacheName:{}", cacheName);
                return null;
            }
            return cache.getIfPresent(key);
        } catch (Exception e) {
            log.error("LocalCacheManager#get fail! cacheName:{}, key:{}, e:{}", 
                    cacheName, key, Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    /**
     * 设置缓存
     *
     * @param cacheName 缓存名称
     * @param key       缓存key
     * @param value     缓存值
     */
    public void put(String cacheName, String key, Object value) {
        try {
            Cache<String, Object> cache = cacheMap.get(cacheName);
            if (cache == null) {
                log.warn("LocalCacheManager#put cache not found! cacheName:{}", cacheName);
                return;
            }
            cache.put(key, value);
        } catch (Exception e) {
            log.error("LocalCacheManager#put fail! cacheName:{}, key:{}, e:{}", 
                    cacheName, key, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 删除缓存
     *
     * @param cacheName 缓存名称
     * @param key       缓存key
     */
    public void invalidate(String cacheName, String key) {
        try {
            Cache<String, Object> cache = cacheMap.get(cacheName);
            if (cache == null) {
                log.warn("LocalCacheManager#invalidate cache not found! cacheName:{}", cacheName);
                return;
            }
            cache.invalidate(key);
        } catch (Exception e) {
            log.error("LocalCacheManager#invalidate fail! cacheName:{}, key:{}, e:{}", 
                    cacheName, key, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 清空指定缓存
     *
     * @param cacheName 缓存名称
     */
    public void invalidateAll(String cacheName) {
        try {
            Cache<String, Object> cache = cacheMap.get(cacheName);
            if (cache == null) {
                log.warn("LocalCacheManager#invalidateAll cache not found! cacheName:{}", cacheName);
                return;
            }
            cache.invalidateAll();
            log.info("LocalCacheManager#invalidateAll success! cacheName:{}", cacheName);
        } catch (Exception e) {
            log.error("LocalCacheManager#invalidateAll fail! cacheName:{}, e:{}", 
                    cacheName, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @param cacheName 缓存名称
     * @return 缓存统计
     */
    public CacheStats getStats(String cacheName) {
        try {
            Cache<String, Object> cache = cacheMap.get(cacheName);
            if (cache == null) {
                log.warn("LocalCacheManager#getStats cache not found! cacheName:{}", cacheName);
                return null;
            }
            return cache.stats();
        } catch (Exception e) {
            log.error("LocalCacheManager#getStats fail! cacheName:{}, e:{}", 
                    cacheName, Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    /**
     * 获取所有缓存的统计信息
     *
     * @return 所有缓存统计
     */
    public Map<String, CacheStats> getAllStats() {
        Map<String, CacheStats> statsMap = new ConcurrentHashMap<>();
        try {
            for (Map.Entry<String, Cache<String, Object>> entry : cacheMap.entrySet()) {
                statsMap.put(entry.getKey(), entry.getValue().stats());
            }
        } catch (Exception e) {
            log.error("LocalCacheManager#getAllStats fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return statsMap;
    }

    /**
     * 获取缓存大小
     *
     * @param cacheName 缓存名称
     * @return 缓存大小
     */
    public long getSize(String cacheName) {
        try {
            Cache<String, Object> cache = cacheMap.get(cacheName);
            if (cache == null) {
                return 0L;
            }
            return cache.estimatedSize();
        } catch (Exception e) {
            log.error("LocalCacheManager#getSize fail! cacheName:{}, e:{}", 
                    cacheName, Throwables.getStackTraceAsString(e));
            return 0L;
        }
    }

    /**
     * 打印所有缓存统计信息
     */
    public void printAllStats() {
        try {
            log.info("========== Local Cache Statistics ==========");
            for (Map.Entry<String, Cache<String, Object>> entry : cacheMap.entrySet()) {
                String cacheName = entry.getKey();
                Cache<String, Object> cache = entry.getValue();
                CacheStats stats = cache.stats();
                
                log.info("Cache: {}, Size: {}, HitRate: {:.2f}%, Hits: {}, Misses: {}", 
                        cacheName, 
                        cache.estimatedSize(),
                        stats.hitRate() * 100,
                        stats.hitCount(),
                        stats.missCount());
            }
            log.info("============================================");
        } catch (Exception e) {
            log.error("LocalCacheManager#printAllStats fail! e:{}", Throwables.getStackTraceAsString(e));
        }
    }
}
