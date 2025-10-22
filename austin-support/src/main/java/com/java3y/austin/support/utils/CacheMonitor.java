package com.java3y.austin.support.utils;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存监控组件
 * 定期收集和上报缓存相关指标
 *
 * @author 3y
 */
@Slf4j
@Component
public class CacheMonitor {

    @Autowired
    private LocalCacheManager localCacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BloomFilterManager bloomFilterManager;

    /**
     * 缓存指标
     */
    private final Map<String, CacheMetrics> metricsMap = new HashMap<>();

    /**
     * 定时打印缓存统计信息（每5分钟）
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void printCacheStats() {
        try {
            log.info("==================== Cache Monitor Report ====================");
            
            // 1. 本地缓存统计
            printLocalCacheStats();
            
            // 2. Redis 连接状态
            printRedisStats();
            
            log.info("==============================================================");
        } catch (Exception e) {
            log.error("CacheMonitor#printCacheStats fail! e:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 打印本地缓存统计
     */
    private void printLocalCacheStats() {
        try {
            Map<String, CacheStats> allStats = localCacheManager.getAllStats();
            
            log.info(">>> Local Cache Statistics:");
            for (Map.Entry<String, CacheStats> entry : allStats.entrySet()) {
                String cacheName = entry.getKey();
                CacheStats stats = entry.getValue();
                long size = localCacheManager.getSize(cacheName);
                
                double hitRate = stats.hitRate() * 100;
                long hitCount = stats.hitCount();
                long missCount = stats.missCount();
                long totalRequests = hitCount + missCount;
                
                log.info("    Cache[{}]: Size={}, HitRate={:.2f}%, Hits={}, Misses={}, Total={}", 
                        cacheName, size, hitRate, hitCount, missCount, totalRequests);
                
                // 记录指标
                recordMetrics(cacheName, hitRate, size, totalRequests);
            }
        } catch (Exception e) {
            log.error("CacheMonitor#printLocalCacheStats fail! e:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 打印 Redis 统计
     */
    private void printRedisStats() {
        try {
            // 检查 Redis 连接状态
            String ping = redisTemplate.getConnectionFactory().getConnection().ping();
            boolean isConnected = "PONG".equals(ping);
            
            log.info(">>> Redis Status: {}", isConnected ? "Connected" : "Disconnected");
            
            if (!isConnected) {
                log.error("Redis connection is down! Please check Redis service.");
            }
        } catch (Exception e) {
            log.error("CacheMonitor#printRedisStats fail! Redis might be down. e:{}", 
                    Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 记录缓存指标
     */
    private void recordMetrics(String cacheName, double hitRate, long size, long totalRequests) {
        CacheMetrics metrics = metricsMap.computeIfAbsent(cacheName, k -> new CacheMetrics());
        metrics.setHitRate(hitRate);
        metrics.setSize(size);
        metrics.setTotalRequests(totalRequests);
        metrics.setLastUpdateTime(System.currentTimeMillis());
    }

    /**
     * 获取指定缓存的指标
     *
     * @param cacheName 缓存名称
     * @return 缓存指标
     */
    public CacheMetrics getMetrics(String cacheName) {
        return metricsMap.get(cacheName);
    }

    /**
     * 获取所有缓存指标
     *
     * @return 所有缓存指标
     */
    public Map<String, CacheMetrics> getAllMetrics() {
        return new HashMap<>(metricsMap);
    }

    /**
     * 检查缓存健康状态
     *
     * @return true表示健康
     */
    public boolean checkHealth() {
        try {
            // 1. 检查 Redis 连接
            String ping = redisTemplate.getConnectionFactory().getConnection().ping();
            if (!"PONG".equals(ping)) {
                log.error("CacheMonitor#checkHealth Redis connection failed!");
                return false;
            }

            // 2. 检查本地缓存命中率
            Map<String, CacheStats> allStats = localCacheManager.getAllStats();
            for (Map.Entry<String, CacheStats> entry : allStats.entrySet()) {
                String cacheName = entry.getKey();
                CacheStats stats = entry.getValue();
                double hitRate = stats.hitRate() * 100;
                
                // 命中率低于50%告警
                if (hitRate < 50 && stats.requestCount() > 100) {
                    log.warn("CacheMonitor#checkHealth Low hit rate! cache:{}, hitRate:{:.2f}%", 
                            cacheName, hitRate);
                }
            }

            return true;
        } catch (Exception e) {
            log.error("CacheMonitor#checkHealth fail! e:{}", Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 缓存指标数据类
     */
    public static class CacheMetrics {
        private double hitRate;
        private long size;
        private long totalRequests;
        private long lastUpdateTime;

        public double getHitRate() {
            return hitRate;
        }

        public void setHitRate(double hitRate) {
            this.hitRate = hitRate;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public void setTotalRequests(long totalRequests) {
            this.totalRequests = totalRequests;
        }

        public long getLastUpdateTime() {
            return lastUpdateTime;
        }

        public void setLastUpdateTime(long lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }

        @Override
        public String toString() {
            return String.format("CacheMetrics{hitRate=%.2f%%, size=%d, totalRequests=%d, lastUpdateTime=%d}",
                    hitRate, size, totalRequests, lastUpdateTime);
        }
    }
}
