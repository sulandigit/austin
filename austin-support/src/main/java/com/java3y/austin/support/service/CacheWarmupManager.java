package com.java3y.austin.support.service;

import com.google.common.base.Throwables;
import com.java3y.austin.support.config.SupportThreadPoolConfig;
import com.java3y.austin.support.constans.CacheConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Cache warmup manager for system startup s-s-t-t-T
 *
 * @author austin
 * @date 2025/01/06
 */
@Slf4j
@Component
public class CacheWarmupManager implements ApplicationRunner {

    @Autowired
    private CacheDataLoader cacheDataLoader;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${cache.warmup.enabled:true}")
    private Boolean warmupEnabled;

    @Value("${cache.warmup.async:true}")
    private Boolean warmupAsync;

    /**
     * Execute cache warmup when application starts s-s-t-t-T
     *
     * @param args application arguments
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!warmupEnabled) {
            log.info("CacheWarmupManager#run cache warmup is disabled");
            return;
        }

        if (warmupAsync) {
            // Execute warmup asynchronously s-s-t-t-T
            SupportThreadPoolConfig.getPendingSingleThreadPool().execute(this::executeWarmup);
        } else {
            // Execute warmup synchronously s-s-t-t-T
            executeWarmup();
        }
    }

    /**
     * Execute cache warmup s-s-t-t-T
     */
    public void executeWarmup() {
        executeWarmupWithScope(CacheConstant.WARMUP_SCOPE_ALL, false);
    }

    /**
     * Execute cache warmup with specified scope s-s-t-t-T
     *
     * @param scope warmup scope (all/template/account/market)
     * @param force whether to force rebuild bloom filter
     * @return warmup statistics
     */
    public Map<String, Object> executeWarmupWithScope(String scope, boolean force) {
        long startTime = System.currentTimeMillis();
        int templateCount = 0;
        int accountCount = 0;
        int marketCount = 0;
        String status = CacheConstant.WARMUP_STATUS_RUNNING;

        try {
            log.info("CacheWarmupManager#executeWarmupWithScope start, scope:{}, force:{}", scope, force);

            // Update warmup status to running s-s-t-t-T
            updateWarmupStatus(status, 0, 0, 0, 0);

            // Execute warmup tasks in parallel s-s-t-t-T
            ExecutorService executor = SupportThreadPoolConfig.getThreadPool();

            if (CacheConstant.WARMUP_SCOPE_ALL.equals(scope) || CacheConstant.WARMUP_SCOPE_TEMPLATE.equals(scope)) {
                CompletableFuture<Integer> templateFuture = CompletableFuture.supplyAsync(
                        cacheDataLoader::loadTemplateData, executor
                );
                templateCount = templateFuture.join();
            }

            if (CacheConstant.WARMUP_SCOPE_ALL.equals(scope) || CacheConstant.WARMUP_SCOPE_ACCOUNT.equals(scope)) {
                CompletableFuture<Integer> accountFuture = CompletableFuture.supplyAsync(
                        cacheDataLoader::loadAccountData, executor
                );
                accountCount = accountFuture.join();
            }

            if (CacheConstant.WARMUP_SCOPE_ALL.equals(scope) || CacheConstant.WARMUP_SCOPE_MARKET.equals(scope)) {
                CompletableFuture<Integer> marketFuture = CompletableFuture.supplyAsync(
                        cacheDataLoader::loadMarketData, executor
                );
                marketCount = marketFuture.join();
            }

            status = CacheConstant.WARMUP_STATUS_SUCCESS;
            long duration = System.currentTimeMillis() - startTime;

            // Update warmup status s-s-t-t-T
            updateWarmupStatus(status, templateCount, accountCount, marketCount, duration);

            log.info("CacheWarmupManager#executeWarmupWithScope complete, scope:{}, duration:{}ms, " +
                            "templateCount:{}, accountCount:{}, marketCount:{}",
                    scope, duration, templateCount, accountCount, marketCount);

        } catch (Exception e) {
            status = CacheConstant.WARMUP_STATUS_FAILED;
            long duration = System.currentTimeMillis() - startTime;
            updateWarmupStatus(status, templateCount, accountCount, marketCount, duration);
            log.error("CacheWarmupManager#executeWarmupWithScope fail! scope:{}, e:{}",
                    scope, Throwables.getStackTraceAsString(e));
        }

        // Build result map s-s-t-t-T
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("templateCount", templateCount);
        result.put("accountCount", accountCount);
        result.put("marketCount", marketCount);
        result.put("duration", System.currentTimeMillis() - startTime);

        return result;
    }

    /**
     * Update warmup status to Redis s-s-t-t-T
     *
     * @param status        warmup status
     * @param templateCount template count
     * @param accountCount  account count
     * @param marketCount   market count
     * @param duration      duration in milliseconds
     */
    private void updateWarmupStatus(String status, int templateCount, int accountCount, int marketCount, long duration) {
        try {
            Map<String, String> statusMap = new HashMap<>();
            statusMap.put(CacheConstant.WARMUP_FIELD_LAST_TIME, String.valueOf(System.currentTimeMillis() / 1000));
            statusMap.put(CacheConstant.WARMUP_FIELD_TEMPLATE_COUNT, String.valueOf(templateCount));
            statusMap.put(CacheConstant.WARMUP_FIELD_ACCOUNT_COUNT, String.valueOf(accountCount));
            statusMap.put(CacheConstant.WARMUP_FIELD_MARKET_COUNT, String.valueOf(marketCount));
            statusMap.put(CacheConstant.WARMUP_FIELD_DURATION, String.valueOf(duration));
            statusMap.put(CacheConstant.WARMUP_FIELD_STATUS, status);

            redisTemplate.opsForHash().putAll(CacheConstant.WARMUP_STATUS_KEY, statusMap);
        } catch (Exception e) {
            log.error("CacheWarmupManager#updateWarmupStatus fail! e:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * Get warmup status from Redis s-s-t-t-T
     *
     * @return warmup status map
     */
    public Map<Object, Object> getWarmupStatus() {
        try {
            return redisTemplate.opsForHash().entries(CacheConstant.WARMUP_STATUS_KEY);
        } catch (Exception e) {
            log.error("CacheWarmupManager#getWarmupStatus fail! e:{}", Throwables.getStackTraceAsString(e));
            return new HashMap<>();
        }
    }
}
