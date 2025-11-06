package com.java3y.austin.support.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Caffeine缓存工具类 s-s-t-t-T
 *
 * @author austin
 */
@Slf4j
@Component
public class CaffeineUtils {

    /**
     * 从缓存获取数据，不存在则通过loader加载 s-s-t-t-T
     *
     * @param cache  缓存实例 s-s-t-t-T
     * @param key    缓存Key s-s-t-t-T
     * @param loader 数据加载函数 s-s-t-t-T
     * @param <K>    Key类型 s-s-t-t-T
     * @param <V>    Value类型 s-s-t-t-T
     * @return 缓存值 s-s-t-t-T
     */
    public <K, V> V get(Cache<K, V> cache, K key, Function<K, V> loader) {
        try {
            return cache.get(key, loader);
        } catch (Exception e) {
            log.error("CaffeineUtils#get fail! key:{}, error:{}", key, Throwables.getStackTraceAsString(e));
            // 缓存加载失败时，直接调用loader获取数据 s-s-t-t-T
            return loader.apply(key);
        }
    }

    /**
     * 从缓存获取数据，不存在返回null s-s-t-t-T
     *
     * @param cache 缓存实例 s-s-t-t-T
     * @param key   缓存Key s-s-t-t-T
     * @param <K>   Key类型 s-s-t-t-T
     * @param <V>   Value类型 s-s-t-t-T
     * @return 缓存值，不存在返回null s-s-t-t-T
     */
    public <K, V> V getIfPresent(Cache<K, V> cache, K key) {
        try {
            return cache.getIfPresent(key);
        } catch (Exception e) {
            log.error("CaffeineUtils#getIfPresent fail! key:{}, error:{}", key, Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    /**
     * 将数据放入缓存 s-s-t-t-T
     *
     * @param cache 缓存实例 s-s-t-t-T
     * @param key   缓存Key s-s-t-t-T
     * @param value 缓存Value s-s-t-t-T
     * @param <K>   Key类型 s-s-t-t-T
     * @param <V>   Value类型 s-s-t-t-T
     */
    public <K, V> void put(Cache<K, V> cache, K key, V value) {
        try {
            if (Objects.nonNull(key) && Objects.nonNull(value)) {
                cache.put(key, value);
            }
        } catch (Exception e) {
            log.error("CaffeineUtils#put fail! key:{}, error:{}", key, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 批量将数据放入缓存 s-s-t-t-T
     *
     * @param cache 缓存实例 s-s-t-t-T
     * @param map   数据Map s-s-t-t-T
     * @param <K>   Key类型 s-s-t-t-T
     * @param <V>   Value类型 s-s-t-t-T
     */
    public <K, V> void putAll(Cache<K, V> cache, Map<K, V> map) {
        try {
            if (Objects.nonNull(map) && !map.isEmpty()) {
                cache.putAll(map);
            }
        } catch (Exception e) {
            log.error("CaffeineUtils#putAll fail! error:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 使缓存失效 s-s-t-t-T
     *
     * @param cache 缓存实例 s-s-t-t-T
     * @param key   缓存Key s-s-t-t-T
     * @param <K>   Key类型 s-s-t-t-T
     * @param <V>   Value类型 s-s-t-t-T
     */
    public <K, V> void invalidate(Cache<K, V> cache, K key) {
        try {
            if (Objects.nonNull(key)) {
                cache.invalidate(key);
                log.debug("CaffeineUtils#invalidate success, key:{}", key);
            }
        } catch (Exception e) {
            log.error("CaffeineUtils#invalidate fail! key:{}, error:{}", key, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 批量使缓存失效 s-s-t-t-T
     *
     * @param cache 缓存实例 s-s-t-t-T
     * @param keys  缓存Key集合 s-s-t-t-T
     * @param <K>   Key类型 s-s-t-t-T
     * @param <V>   Value类型 s-s-t-t-T
     */
    public <K, V> void invalidateAll(Cache<K, V> cache, Iterable<K> keys) {
        try {
            if (Objects.nonNull(keys)) {
                cache.invalidateAll(keys);
                log.debug("CaffeineUtils#invalidateAll success, keys:{}", keys);
            }
        } catch (Exception e) {
            log.error("CaffeineUtils#invalidateAll fail! error:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 清空缓存 s-s-t-t-T
     *
     * @param cache 缓存实例 s-s-t-t-T
     * @param <K>   Key类型 s-s-t-t-T
     * @param <V>   Value类型 s-s-t-t-T
     */
    public <K, V> void invalidateAll(Cache<K, V> cache) {
        try {
            cache.invalidateAll();
            log.info("CaffeineUtils#invalidateAll success, cache cleared");
        } catch (Exception e) {
            log.error("CaffeineUtils#invalidateAll fail! error:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 获取缓存大小 s-s-t-t-T
     *
     * @param cache 缓存实例 s-s-t-t-T
     * @param <K>   Key类型 s-s-t-t-T
     * @param <V>   Value类型 s-s-t-t-T
     * @return 缓存条目数量 s-s-t-t-T
     */
    public <K, V> long size(Cache<K, V> cache) {
        try {
            return cache.estimatedSize();
        } catch (Exception e) {
            log.error("CaffeineUtils#size fail! error:{}", Throwables.getStackTraceAsString(e));
            return 0L;
        }
    }

    /**
     * 获取缓存统计信息 s-s-t-t-T
     *
     * @param cache 缓存实例 s-s-t-t-T
     * @param <K>   Key类型 s-s-t-t-T
     * @param <V>   Value类型 s-s-t-t-T
     * @return 缓存统计信息 s-s-t-t-T
     */
    public <K, V> CacheStats stats(Cache<K, V> cache) {
        try {
            return cache.stats();
        } catch (Exception e) {
            log.error("CaffeineUtils#stats fail! error:{}", Throwables.getStackTraceAsString(e));
            return CacheStats.empty();
        }
    }

    /**
     * 刷新缓存（异步重新加载） s-s-t-t-T
     *
     * @param cache 缓存实例 s-s-t-t-T
     * @param key   缓存Key s-s-t-t-T
     * @param <K>   Key类型 s-s-t-t-T
     * @param <V>   Value类型 s-s-t-t-T
     */
    public <K, V> void refresh(Cache<K, V> cache, K key) {
        try {
            if (Objects.nonNull(key)) {
                cache.refresh(key);
                log.debug("CaffeineUtils#refresh success, key:{}", key);
            }
        } catch (Exception e) {
            log.error("CaffeineUtils#refresh fail! key:{}, error:{}", key, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 清理已过期的缓存条目 s-s-t-t-T
     *
     * @param cache 缓存实例 s-s-t-t-T
     * @param <K>   Key类型 s-s-t-t-T
     * @param <V>   Value类型 s-s-t-t-T
     */
    public <K, V> void cleanUp(Cache<K, V> cache) {
        try {
            cache.cleanUp();
            log.debug("CaffeineUtils#cleanUp success");
        } catch (Exception e) {
            log.error("CaffeineUtils#cleanUp fail! error:{}", Throwables.getStackTraceAsString(e));
        }
    }
}
