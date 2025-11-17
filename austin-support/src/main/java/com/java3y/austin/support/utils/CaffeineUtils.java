package com.java3y.austin.support.utils;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 缓存工具类
 * 提供预设的缓存配置模板和统一的缓存统计信息输出格式
 *
 * @author austin
 */
@Slf4j
public class CaffeineUtils {

    private CaffeineUtils() {
    }

    /**
     * 创建配置缓存模板
     * 适用于动态配置场景
     *
     * @param loader 缓存加载器
     * @param <K>    键类型
     * @param <V>    值类型
     * @return LoadingCache 实例
     */
    public static <K, V> LoadingCache<K, V> buildConfigCache(CacheLoader<K, V> loader) {
        return Caffeine.newBuilder()
                // 最大容量 1000
                .maximumSize(1000)
                // 写入后 30 分钟过期
                .expireAfterWrite(30, TimeUnit.MINUTES)
                // 启用统计
                .recordStats()
                .build(loader);
    }

    /**
     * 创建对象缓存模板
     * 适用于重量级对象场景
     *
     * @param loader 缓存加载器
     * @param <K>    键类型
     * @param <V>    值类型
     * @return LoadingCache 实例
     */
    public static <K, V> LoadingCache<K, V> buildObjectCache(CacheLoader<K, V> loader) {
        return Caffeine.newBuilder()
                // 最大容量 500
                .maximumSize(500)
                // 访问后 1 小时过期
                .expireAfterAccess(1, TimeUnit.HOURS)
                // 写入后 30 分钟刷新
                .refreshAfterWrite(30, TimeUnit.MINUTES)
                // 启用弱值引用
                .weakValues()
                // 启用统计
                .recordStats()
                .build(loader);
    }

    /**
     * 创建自定义配置的缓存
     *
     * @param maxSize          最大容量
     * @param expireAfterWrite 写入后过期时间（分钟）
     * @param loader           缓存加载器
     * @param <K>              键类型
     * @param <V>              值类型
     * @return LoadingCache 实例
     */
    public static <K, V> LoadingCache<K, V> buildCustomCache(long maxSize,
                                                              long expireAfterWrite,
                                                              CacheLoader<K, V> loader) {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                .recordStats()
                .build(loader);
    }

    /**
     * 输出缓存统计信息
     *
     * @param cacheName 缓存名称
     * @param cache     缓存实例
     */
    public static <K, V> void logCacheStats(String cacheName, LoadingCache<K, V> cache) {
        CacheStats stats = cache.stats();
        log.info("CaffeineCache stats: name={}, hitRate={:.2f}%, requestCount={}, " +
                        "loadSuccessCount={}, loadFailureCount={}, evictionCount={}, " +
                        "avgLoadPenalty={}ms, size={}",
                cacheName,
                stats.hitRate() * 100,
                stats.requestCount(),
                stats.loadSuccessCount(),
                stats.loadFailureCount(),
                stats.evictionCount(),
                stats.averageLoadPenalty() / 1_000_000,
                cache.estimatedSize());
    }

    /**
     * 格式化缓存统计信息为字符串
     *
     * @param cacheName 缓存名称
     * @param cache     缓存实例
     * @return 格式化的统计信息
     */
    public static <K, V> String formatCacheStats(String cacheName, LoadingCache<K, V> cache) {
        CacheStats stats = cache.stats();
        return String.format("CaffeineCache stats: name=%s, hitRate=%.2f%%, requestCount=%d, " +
                        "loadSuccessCount=%d, loadFailureCount=%d, evictionCount=%d, " +
                        "avgLoadPenalty=%.2fms, size=%d",
                cacheName,
                stats.hitRate() * 100,
                stats.requestCount(),
                stats.loadSuccessCount(),
                stats.loadFailureCount(),
                stats.evictionCount(),
                stats.averageLoadPenalty() / 1_000_000.0,
                cache.estimatedSize());
    }

    /**
     * 初始化日志
     *
     * @param cacheName 缓存名称
     * @param maxSize   最大容量
     * @param config    配置描述
     */
    public static void logCacheInit(String cacheName, long maxSize, String config) {
        log.info("CaffeineCache initialized: name={}, maxSize={}, config={}",
                cacheName, maxSize, config);
    }
}
