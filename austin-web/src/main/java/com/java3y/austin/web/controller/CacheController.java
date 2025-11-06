package com.java3y.austin.web.controller;

import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.support.constans.CacheConstant;
import com.java3y.austin.support.service.BloomFilterManager;
import com.java3y.austin.support.service.CacheWarmupManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache management API controller s-s-t-t-T
 *
 * @author austin
 * @date 2025/01/06
 */
@Slf4j
@RestController
@RequestMapping("/api/cache")
@Api(tags = "缓存管理接口")
public class CacheController {

    @Autowired
    private CacheWarmupManager cacheWarmupManager;

    @Autowired
    private BloomFilterManager bloomFilterManager;

    /**
     * Trigger cache warmup s-s-t-t-T
     *
     * @param scope warmup scope (all/template/account/market)
     * @param force whether to force rebuild bloom filter
     * @return warmup result
     */
    @ApiOperation("触发缓存预热")
    @PostMapping("/warmup")
    public BasicResultVO triggerWarmup(
            @ApiParam(value = "预热范围", required = true)
            @RequestParam(defaultValue = CacheConstant.WARMUP_SCOPE_ALL) String scope,
            @ApiParam("是否强制重建")
            @RequestParam(defaultValue = "false") Boolean force) {
        try {
            log.info("CacheController#triggerWarmup scope:{}, force:{}", scope, force);
            Map<String, Object> result = cacheWarmupManager.executeWarmupWithScope(scope, force);
            return BasicResultVO.success(result);
        } catch (Exception e) {
            log.error("CacheController#triggerWarmup fail! scope:{}, e:{}", scope, e.getMessage());
            return BasicResultVO.fail("缓存预热失败: " + e.getMessage());
        }
    }

    /**
     * Query warmup status s-s-t-t-T
     *
     * @return warmup status
     */
    @ApiOperation("查询预热状态")
    @GetMapping("/warmup/status")
    public BasicResultVO getWarmupStatus() {
        try {
            Map<Object, Object> status = cacheWarmupManager.getWarmupStatus();
            return BasicResultVO.success(status);
        } catch (Exception e) {
            log.error("CacheController#getWarmupStatus fail! e:{}", e.getMessage());
            return BasicResultVO.fail("查询预热状态失败: " + e.getMessage());
        }
    }

    /**
     * Rebuild bloom filter s-s-t-t-T
     *
     * @param filterName filter name (template/account/market)
     * @return rebuild result
     */
    @ApiOperation("重建布隆过滤器")
    @PostMapping("/bloomfilter/rebuild")
    public BasicResultVO rebuildBloomFilter(
            @ApiParam(value = "过滤器名称", required = true)
            @RequestParam String filterName) {
        try {
            log.info("CacheController#rebuildBloomFilter filterName:{}", filterName);
            long elementCount = 0;

            switch (filterName) {
                case CacheConstant.WARMUP_SCOPE_TEMPLATE:
                    elementCount = bloomFilterManager.rebuildTemplateBloomFilter();
                    break;
                case CacheConstant.WARMUP_SCOPE_ACCOUNT:
                    elementCount = bloomFilterManager.rebuildAccountBloomFilter();
                    break;
                case CacheConstant.WARMUP_SCOPE_MARKET:
                    elementCount = bloomFilterManager.rebuildMarketBloomFilter();
                    break;
                default:
                    return BasicResultVO.fail("不支持的过滤器名称: " + filterName);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("filterName", filterName);
            result.put("elementCount", elementCount);
            return BasicResultVO.success(result);
        } catch (Exception e) {
            log.error("CacheController#rebuildBloomFilter fail! filterName:{}, e:{}", filterName, e.getMessage());
            return BasicResultVO.fail("重建布隆过滤器失败: " + e.getMessage());
        }
    }

    /**
     * Query bloom filter statistics s-s-t-t-T
     *
     * @return bloom filter statistics
     */
    @ApiOperation("查询布隆过滤器统计")
    @GetMapping("/bloomfilter/stats")
    public BasicResultVO getBloomFilterStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("templateCount", bloomFilterManager.getTemplateCount());
            stats.put("accountCount", bloomFilterManager.getAccountCount());
            stats.put("marketCount", bloomFilterManager.getMarketCount());
            return BasicResultVO.success(stats);
        } catch (Exception e) {
            log.error("CacheController#getBloomFilterStats fail! e:{}", e.getMessage());
            return BasicResultVO.fail("查询布隆过滤器统计失败: " + e.getMessage());
        }
    }
}
