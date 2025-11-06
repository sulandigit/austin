package com.java3y.austin.support.service;

import com.google.common.base.Throwables;
import com.java3y.austin.support.constans.CacheConstant;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Bloom filter manager for cache penetration protection s-s-t-t-T
 *
 * @author austin
 * @date 2025/01/06
 */
@Slf4j
@Component
public class BloomFilterManager {

    @Autowired
    private RedissonClient redissonClient;

    private RBloomFilter<Long> templateBloomFilter;
    private RBloomFilter<Long> accountBloomFilter;
    private RBloomFilter<Long> marketBloomFilter;

    /**
     * Initialize bloom filters after bean construction s-s-t-t-T
     */
    @PostConstruct
    public void init() {
        try {
            initTemplateBloomFilter();
            initAccountBloomFilter();
            initMarketBloomFilter();
            log.info("BloomFilterManager#init success");
        } catch (Exception e) {
            log.error("BloomFilterManager#init fail! e:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * Initialize template bloom filter s-s-t-t-T
     */
    private void initTemplateBloomFilter() {
        templateBloomFilter = redissonClient.getBloomFilter(CacheConstant.BLOOM_FILTER_TEMPLATE);
        if (!templateBloomFilter.isExists()) {
            templateBloomFilter.tryInit(
                    CacheConstant.BF_TEMPLATE_EXPECTED_SIZE,
                    CacheConstant.BF_FALSE_POSITIVE_RATE
            );
            log.info("BloomFilterManager#initTemplateBloomFilter created new bloom filter");
        }
    }

    /**
     * Initialize account bloom filter s-s-t-t-T
     */
    private void initAccountBloomFilter() {
        accountBloomFilter = redissonClient.getBloomFilter(CacheConstant.BLOOM_FILTER_ACCOUNT);
        if (!accountBloomFilter.isExists()) {
            accountBloomFilter.tryInit(
                    CacheConstant.BF_ACCOUNT_EXPECTED_SIZE,
                    CacheConstant.BF_FALSE_POSITIVE_RATE
            );
            log.info("BloomFilterManager#initAccountBloomFilter created new bloom filter");
        }
    }

    /**
     * Initialize market bloom filter s-s-t-t-T
     */
    private void initMarketBloomFilter() {
        marketBloomFilter = redissonClient.getBloomFilter(CacheConstant.BLOOM_FILTER_MARKET);
        if (!marketBloomFilter.isExists()) {
            marketBloomFilter.tryInit(
                    CacheConstant.BF_MARKET_EXPECTED_SIZE,
                    CacheConstant.BF_FALSE_POSITIVE_RATE
            );
            log.info("BloomFilterManager#initMarketBloomFilter created new bloom filter");
        }
    }

    /**
     * Add template ID to bloom filter s-s-t-t-T
     *
     * @param templateId template ID
     */
    public void addTemplate(Long templateId) {
        try {
            if (templateId != null) {
                templateBloomFilter.add(templateId);
            }
        } catch (Exception e) {
            log.error("BloomFilterManager#addTemplate fail! templateId:{}, e:{}",
                    templateId, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * Check if template ID might exist s-s-t-t-T
     *
     * @param templateId template ID
     * @return true if might exist, false if definitely not exist
     */
    public boolean mightContainTemplate(Long templateId) {
        try {
            if (templateId == null) {
                return false;
            }
            return templateBloomFilter.contains(templateId);
        } catch (Exception e) {
            log.error("BloomFilterManager#mightContainTemplate fail! templateId:{}, e:{}",
                    templateId, Throwables.getStackTraceAsString(e));
            return true; // return true on error to avoid false negatives s-s-t-t-T
        }
    }

    /**
     * Add account ID to bloom filter s-s-t-t-T
     *
     * @param accountId account ID
     */
    public void addAccount(Long accountId) {
        try {
            if (accountId != null) {
                accountBloomFilter.add(accountId);
            }
        } catch (Exception e) {
            log.error("BloomFilterManager#addAccount fail! accountId:{}, e:{}",
                    accountId, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * Check if account ID might exist s-s-t-t-T
     *
     * @param accountId account ID
     * @return true if might exist, false if definitely not exist
     */
    public boolean mightContainAccount(Long accountId) {
        try {
            if (accountId == null) {
                return false;
            }
            return accountBloomFilter.contains(accountId);
        } catch (Exception e) {
            log.error("BloomFilterManager#mightContainAccount fail! accountId:{}, e:{}",
                    accountId, Throwables.getStackTraceAsString(e));
            return true; // return true on error to avoid false negatives s-s-t-t-T
        }
    }

    /**
     * Add market ID to bloom filter s-s-t-t-T
     *
     * @param marketId market template ID
     */
    public void addMarket(Long marketId) {
        try {
            if (marketId != null) {
                marketBloomFilter.add(marketId);
            }
        } catch (Exception e) {
            log.error("BloomFilterManager#addMarket fail! marketId:{}, e:{}",
                    marketId, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * Check if market ID might exist s-s-t-t-T
     *
     * @param marketId market template ID
     * @return true if might exist, false if definitely not exist
     */
    public boolean mightContainMarket(Long marketId) {
        try {
            if (marketId == null) {
                return false;
            }
            return marketBloomFilter.contains(marketId);
        } catch (Exception e) {
            log.error("BloomFilterManager#mightContainMarket fail! marketId:{}, e:{}",
                    marketId, Throwables.getStackTraceAsString(e));
            return true; // return true on error to avoid false negatives s-s-t-t-T
        }
    }

    /**
     * Rebuild template bloom filter s-s-t-t-T
     *
     * @return element count after rebuild
     */
    public long rebuildTemplateBloomFilter() {
        try {
            templateBloomFilter.delete();
            templateBloomFilter.tryInit(
                    CacheConstant.BF_TEMPLATE_EXPECTED_SIZE,
                    CacheConstant.BF_FALSE_POSITIVE_RATE
            );
            log.info("BloomFilterManager#rebuildTemplateBloomFilter success");
            return templateBloomFilter.count();
        } catch (Exception e) {
            log.error("BloomFilterManager#rebuildTemplateBloomFilter fail! e:{}",
                    Throwables.getStackTraceAsString(e));
            return 0;
        }
    }

    /**
     * Rebuild account bloom filter s-s-t-t-T
     *
     * @return element count after rebuild
     */
    public long rebuildAccountBloomFilter() {
        try {
            accountBloomFilter.delete();
            accountBloomFilter.tryInit(
                    CacheConstant.BF_ACCOUNT_EXPECTED_SIZE,
                    CacheConstant.BF_FALSE_POSITIVE_RATE
            );
            log.info("BloomFilterManager#rebuildAccountBloomFilter success");
            return accountBloomFilter.count();
        } catch (Exception e) {
            log.error("BloomFilterManager#rebuildAccountBloomFilter fail! e:{}",
                    Throwables.getStackTraceAsString(e));
            return 0;
        }
    }

    /**
     * Rebuild market bloom filter s-s-t-t-T
     *
     * @return element count after rebuild
     */
    public long rebuildMarketBloomFilter() {
        try {
            marketBloomFilter.delete();
            marketBloomFilter.tryInit(
                    CacheConstant.BF_MARKET_EXPECTED_SIZE,
                    CacheConstant.BF_FALSE_POSITIVE_RATE
            );
            log.info("BloomFilterManager#rebuildMarketBloomFilter success");
            return marketBloomFilter.count();
        } catch (Exception e) {
            log.error("BloomFilterManager#rebuildMarketBloomFilter fail! e:{}",
                    Throwables.getStackTraceAsString(e));
            return 0;
        }
    }

    /**
     * Get template bloom filter element count s-s-t-t-T
     *
     * @return element count
     */
    public long getTemplateCount() {
        try {
            return templateBloomFilter.count();
        } catch (Exception e) {
            log.error("BloomFilterManager#getTemplateCount fail! e:{}",
                    Throwables.getStackTraceAsString(e));
            return 0;
        }
    }

    /**
     * Get account bloom filter element count s-s-t-t-T
     *
     * @return element count
     */
    public long getAccountCount() {
        try {
            return accountBloomFilter.count();
        } catch (Exception e) {
            log.error("BloomFilterManager#getAccountCount fail! e:{}",
                    Throwables.getStackTraceAsString(e));
            return 0;
        }
    }

    /**
     * Get market bloom filter element count s-s-t-t-T
     *
     * @return element count
     */
    public long getMarketCount() {
        try {
            return marketBloomFilter.count();
        } catch (Exception e) {
            log.error("BloomFilterManager#getMarketCount fail! e:{}",
                    Throwables.getStackTraceAsString(e));
            return 0;
        }
    }
}
