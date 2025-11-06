package com.java3y.austin.cron.handler;

import com.java3y.austin.support.constans.CacheConstant;
import com.java3y.austin.support.service.CacheWarmupManager;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Cache warmup scheduled task handler s-s-t-t-T
 *
 * @author austin
 * @date 2025/01/06
 */
@Slf4j
@Service
public class CacheWarmupHandler {

    @Autowired
    private CacheWarmupManager cacheWarmupManager;

    /**
     * Full cache warmup job (daily at 2:00 AM) s-s-t-t-T
     * Cron expression: 0 0 2 * * ?
     */
    @XxlJob("cacheWarmupJob")
    public void cacheWarmupJob() {
        log.info("CacheWarmupHandler#cacheWarmupJob start");
        try {
            Map<String, Object> result = cacheWarmupManager.executeWarmupWithScope(
                    CacheConstant.WARMUP_SCOPE_ALL, false
            );
            log.info("CacheWarmupHandler#cacheWarmupJob complete, result:{}", result);
        } catch (Exception e) {
            log.error("CacheWarmupHandler#cacheWarmupJob fail! e:{}", e.getMessage(), e);
        }
    }

    /**
     * Incremental cache warmup job (every 30 minutes) s-s-t-t-T
     * Cron expression: 0 *\/30 * * * ?
     */
    @XxlJob("incrementalWarmupJob")
    public void incrementalWarmupJob() {
        log.info("CacheWarmupHandler#incrementalWarmupJob start");
        try {
            // Execute warmup for all scopes (incremental) s-s-t-t-T
            Map<String, Object> result = cacheWarmupManager.executeWarmupWithScope(
                    CacheConstant.WARMUP_SCOPE_ALL, false
            );
            log.info("CacheWarmupHandler#incrementalWarmupJob complete, result:{}", result);
        } catch (Exception e) {
            log.error("CacheWarmupHandler#incrementalWarmupJob fail! e:{}", e.getMessage(), e);
        }
    }

    /**
     * Template cache warmup job s-s-t-t-T
     */
    @XxlJob("templateWarmupJob")
    public void templateWarmupJob() {
        log.info("CacheWarmupHandler#templateWarmupJob start");
        try {
            Map<String, Object> result = cacheWarmupManager.executeWarmupWithScope(
                    CacheConstant.WARMUP_SCOPE_TEMPLATE, false
            );
            log.info("CacheWarmupHandler#templateWarmupJob complete, result:{}", result);
        } catch (Exception e) {
            log.error("CacheWarmupHandler#templateWarmupJob fail! e:{}", e.getMessage(), e);
        }
    }

    /**
     * Account cache warmup job s-s-t-t-T
     */
    @XxlJob("accountWarmupJob")
    public void accountWarmupJob() {
        log.info("CacheWarmupHandler#accountWarmupJob start");
        try {
            Map<String, Object> result = cacheWarmupManager.executeWarmupWithScope(
                    CacheConstant.WARMUP_SCOPE_ACCOUNT, false
            );
            log.info("CacheWarmupHandler#accountWarmupJob complete, result:{}", result);
        } catch (Exception e) {
            log.error("CacheWarmupHandler#accountWarmupJob fail! e:{}", e.getMessage(), e);
        }
    }

    /**
     * Market cache warmup job s-s-t-t-T
     */
    @XxlJob("marketWarmupJob")
    public void marketWarmupJob() {
        log.info("CacheWarmupHandler#marketWarmupJob start");
        try {
            Map<String, Object> result = cacheWarmupManager.executeWarmupWithScope(
                    CacheConstant.WARMUP_SCOPE_MARKET, false
            );
            log.info("CacheWarmupHandler#marketWarmupJob complete, result:{}", result);
        } catch (Exception e) {
            log.error("CacheWarmupHandler#marketWarmupJob fail! e:{}", e.getMessage(), e);
        }
    }
}
