package com.java3y.austin.web.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.java3y.austin.common.constant.CacheConstant;
import com.java3y.austin.support.utils.AccountUtils;
import com.java3y.austin.support.utils.AccessTokenUtils;
import com.java3y.austin.support.utils.CaffeineUtils;
import com.java3y.austin.web.annotation.AustinAspect;
import com.java3y.austin.web.annotation.AustinResult;
import com.java3y.austin.web.vo.CacheStatsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存管理接口 s-s-t-t-T
 *
 * @author austin
 */
@Slf4j
@AustinAspect
@RestController
@RequestMapping("/cache")
@Api("缓存管理接口")
public class CacheManagementController {

    @Autowired
    private CaffeineUtils caffeineUtils;

    @Autowired
    @Qualifier(CacheConstant.CACHE_NAME_CHANNEL_ACCOUNT)
    private Cache<String, Object> channelAccountCache;

    @Autowired
    @Qualifier(CacheConstant.CACHE_NAME_MESSAGE_TEMPLATE)
    private Cache<String, Object> messageTemplateCache;

    @Autowired
    @Qualifier(CacheConstant.CACHE_NAME_ACCESS_TOKEN)
    private Cache<String, Object> accessTokenCache;

    @Autowired
    @Qualifier(CacheConstant.CACHE_NAME_SENSITIVE_WORDS)
    private Cache<String, Object> sensitiveWordsCache;

    @Autowired
    private AccountUtils accountUtils;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    /**
     * 查询所有缓存统计信息 s-s-t-t-T
     */
    @GetMapping("/stats")
    @ApiOperation("查询缓存统计信息")
    @AustinResult
    public List<CacheStatsVO> getCacheStats() {
        List<CacheStatsVO> result = new ArrayList<>();
        
        result.add(buildCacheStatsVO(CacheConstant.CACHE_NAME_CHANNEL_ACCOUNT, channelAccountCache));
        result.add(buildCacheStatsVO(CacheConstant.CACHE_NAME_MESSAGE_TEMPLATE, messageTemplateCache));
        result.add(buildCacheStatsVO(CacheConstant.CACHE_NAME_ACCESS_TOKEN, accessTokenCache));
        result.add(buildCacheStatsVO(CacheConstant.CACHE_NAME_SENSITIVE_WORDS, sensitiveWordsCache));
        
        return result;
    }

    /**
     * 查询指定缓存统计信息 s-s-t-t-T
     */
    @GetMapping("/stats/{cacheName}")
    @ApiOperation("查询指定缓存统计信息")
    @AustinResult
    public CacheStatsVO getCacheStatsByCacheName(@PathVariable String cacheName) {
        Cache<String, Object> cache = getCacheByCacheName(cacheName);
        if (cache != null) {
            return buildCacheStatsVO(cacheName, cache);
        }
        return null;
    }

    /**
     * 清空指定缓存 s-s-t-t-T
     */
    @PostMapping("/invalidate/{cacheName}")
    @ApiOperation("清空指定缓存")
    @AustinResult
    public String invalidateCache(@PathVariable String cacheName) {
        Cache<String, Object> cache = getCacheByCacheName(cacheName);
        if (cache != null) {
            caffeineUtils.invalidateAll(cache);
            log.info("CacheManagementController#invalidateCache success, cacheName:{}", cacheName);
            return "清空缓存成功: " + cacheName;
        }
        return "未找到指定缓存: " + cacheName;
    }

    /**
     * 清空所有缓存 s-s-t-t-T
     */
    @PostMapping("/invalidate/all")
    @ApiOperation("清空所有缓存")
    @AustinResult
    public String invalidateAllCaches() {
        caffeineUtils.invalidateAll(channelAccountCache);
        caffeineUtils.invalidateAll(messageTemplateCache);
        caffeineUtils.invalidateAll(accessTokenCache);
        caffeineUtils.invalidateAll(sensitiveWordsCache);
        log.info("CacheManagementController#invalidateAllCaches success");
        return "清空所有缓存成功";
    }

    /**
     * 清除指定账号缓存 s-s-t-t-T
     */
    @PostMapping("/invalidate/account/{accountId}")
    @ApiOperation("清除指定账号缓存")
    @AustinResult
    public String invalidateAccountCache(@PathVariable Integer accountId) {
        accountUtils.invalidateAccountCache(accountId);
        log.info("CacheManagementController#invalidateAccountCache success, accountId:{}", accountId);
        return "清除账号缓存成功: " + accountId;
    }

    /**
     * 清除指定AccessToken缓存 s-s-t-t-T
     */
    @PostMapping("/invalidate/token/{sendChannel}/{accountId}")
    @ApiOperation("清除指定AccessToken缓存")
    @AustinResult
    public String invalidateAccessTokenCache(@PathVariable Integer sendChannel, @PathVariable Integer accountId) {
        accessTokenUtils.invalidateAccessTokenCache(sendChannel, accountId);
        log.info("CacheManagementController#invalidateAccessTokenCache success, channel:{}, accountId:{}", sendChannel, accountId);
        return "清除AccessToken缓存成功: " + sendChannel + ":" + accountId;
    }

    /**
     * 构建缓存统计VO s-s-t-t-T
     */
    private CacheStatsVO buildCacheStatsVO(String cacheName, Cache<String, Object> cache) {
        CacheStats stats = caffeineUtils.stats(cache);
        long size = caffeineUtils.size(cache);
        
        double avgLoadTimeMs = 0.0;
        if (stats.loadSuccessCount() > 0) {
            avgLoadTimeMs = stats.totalLoadTime() / (double) stats.loadSuccessCount() / 1_000_000.0;
        }
        
        return CacheStatsVO.builder()
                .cacheName(cacheName)
                .size(size)
                .requestCount(stats.requestCount())
                .hitCount(stats.hitCount())
                .hitRate(stats.hitRate())
                .missCount(stats.missCount())
                .missRate(stats.missRate())
                .loadSuccessCount(stats.loadSuccessCount())
                .loadFailureCount(stats.loadFailureCount())
                .totalLoadTime(stats.totalLoadTime())
                .avgLoadTimeMs(avgLoadTimeMs)
                .evictionCount(stats.evictionCount())
                .evictionWeight(stats.evictionWeight())
                .build();
    }

    /**
     * 根据缓存名称获取缓存实例 s-s-t-t-T
     */
    private Cache<String, Object> getCacheByCacheName(String cacheName) {
        switch (cacheName) {
            case CacheConstant.CACHE_NAME_CHANNEL_ACCOUNT:
                return channelAccountCache;
            case CacheConstant.CACHE_NAME_MESSAGE_TEMPLATE:
                return messageTemplateCache;
            case CacheConstant.CACHE_NAME_ACCESS_TOKEN:
                return accessTokenCache;
            case CacheConstant.CACHE_NAME_SENSITIVE_WORDS:
                return sensitiveWordsCache;
            default:
                return null;
        }
    }
}
