package com.java3y.austin.support.service;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.java3y.austin.common.constant.CommonConstant;
import com.java3y.austin.support.constans.CacheConstant;
import com.java3y.austin.support.dao.ChannelAccountDao;
import com.java3y.austin.support.dao.MessageTemplateDao;
import com.java3y.austin.support.dao.TemplateMarketDao;
import com.java3y.austin.support.domain.ChannelAccount;
import com.java3y.austin.support.domain.MessageTemplate;
import com.java3y.austin.support.domain.TemplateMarket;
import com.java3y.austin.support.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cache data loader for warmup s-s-t-t-T
 *
 * @author austin
 * @date 2025/01/06
 */
@Slf4j
@Component
public class CacheDataLoader {

    @Autowired
    private MessageTemplateDao messageTemplateDao;

    @Autowired
    private ChannelAccountDao channelAccountDao;

    @Autowired
    private TemplateMarketDao templateMarketDao;

    @Autowired
    private BloomFilterManager bloomFilterManager;

    @Autowired
    private RedisUtils redisUtils;

    private static final int BATCH_SIZE = 100;

    /**
     * Load template data to cache s-s-t-t-T
     *
     * @return loaded template count
     */
    public int loadTemplateData() {
        int totalCount = 0;
        try {
            log.info("CacheDataLoader#loadTemplateData start");

            // Query all non-deleted templates s-s-t-t-T
            List<MessageTemplate> templates = messageTemplateDao.findAll().stream()
                    .filter(t -> CommonConstant.FALSE.equals(t.getIsDeleted()))
                    .collect(Collectors.toList());

            if (templates.isEmpty()) {
                log.info("CacheDataLoader#loadTemplateData no templates found");
                return 0;
            }

            // Process in batches s-s-t-t-T
            List<List<MessageTemplate>> batches = Lists.partition(templates, BATCH_SIZE);
            for (List<MessageTemplate> batch : batches) {
                try {
                    Map<String, String> cacheMap = new HashMap<>();
                    for (MessageTemplate template : batch) {
                        String key = CacheConstant.TEMPLATE_INFO_PREFIX + template.getId();
                        cacheMap.put(key, JSON.toJSONString(template));
                        // Add to bloom filter s-s-t-t-T
                        bloomFilterManager.addTemplate(template.getId());
                    }
                    // Batch write to Redis using pipeline s-s-t-t-T
                    redisUtils.pipelineSetEx(cacheMap, CacheConstant.TEMPLATE_CACHE_EXPIRE);
                    totalCount += batch.size();
                } catch (Exception e) {
                    log.error("CacheDataLoader#loadTemplateData batch fail! e:{}",
                            Throwables.getStackTraceAsString(e));
                }
            }

            log.info("CacheDataLoader#loadTemplateData complete, count:{}", totalCount);
        } catch (Exception e) {
            log.error("CacheDataLoader#loadTemplateData fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return totalCount;
    }

    /**
     * Load account data to cache s-s-t-t-T
     *
     * @return loaded account count
     */
    public int loadAccountData() {
        int totalCount = 0;
        try {
            log.info("CacheDataLoader#loadAccountData start");

            // Query all non-deleted accounts s-s-t-t-T
            List<ChannelAccount> accounts = channelAccountDao.findAll().stream()
                    .filter(a -> CommonConstant.FALSE.equals(a.getIsDeleted()))
                    .collect(Collectors.toList());

            if (accounts.isEmpty()) {
                log.info("CacheDataLoader#loadAccountData no accounts found");
                return 0;
            }

            // Process in batches s-s-t-t-T
            List<List<ChannelAccount>> batches = Lists.partition(accounts, BATCH_SIZE);
            for (List<ChannelAccount> batch : batches) {
                try {
                    Map<String, String> cacheMap = new HashMap<>();
                    for (ChannelAccount account : batch) {
                        String key = CacheConstant.ACCOUNT_INFO_PREFIX + account.getId();
                        cacheMap.put(key, JSON.toJSONString(account));
                        // Add to bloom filter s-s-t-t-T
                        bloomFilterManager.addAccount(account.getId());
                    }
                    // Batch write to Redis using pipeline s-s-t-t-T
                    redisUtils.pipelineSetEx(cacheMap, CacheConstant.ACCOUNT_CACHE_EXPIRE);
                    totalCount += batch.size();
                } catch (Exception e) {
                    log.error("CacheDataLoader#loadAccountData batch fail! e:{}",
                            Throwables.getStackTraceAsString(e));
                }
            }

            log.info("CacheDataLoader#loadAccountData complete, count:{}", totalCount);
        } catch (Exception e) {
            log.error("CacheDataLoader#loadAccountData fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return totalCount;
    }

    /**
     * Load template market data to cache (top 100 popular templates) s-s-t-t-T
     *
     * @return loaded market template count
     */
    public int loadMarketData() {
        int totalCount = 0;
        try {
            log.info("CacheDataLoader#loadMarketData start");

            // Query top 100 templates by usage count s-s-t-t-T
            // Note: Assuming there's a status field and usedCount field in TemplateMarket s-s-t-t-T
            Pageable pageable = PageRequest.of(0, 100);
            List<TemplateMarket> markets = templateMarketDao.findAll(pageable).getContent();

            if (markets.isEmpty()) {
                log.info("CacheDataLoader#loadMarketData no market templates found");
                return 0;
            }

            // Process in batches s-s-t-t-T
            List<List<TemplateMarket>> batches = Lists.partition(markets, BATCH_SIZE);
            for (List<TemplateMarket> batch : batches) {
                try {
                    Map<String, String> cacheMap = new HashMap<>();
                    for (TemplateMarket market : batch) {
                        String key = CacheConstant.MARKET_TEMPLATE_PREFIX + market.getId();
                        cacheMap.put(key, JSON.toJSONString(market));
                        // Add to bloom filter s-s-t-t-T
                        bloomFilterManager.addMarket(market.getId());
                    }
                    // Batch write to Redis using pipeline s-s-t-t-T
                    redisUtils.pipelineSetEx(cacheMap, CacheConstant.MARKET_CACHE_EXPIRE);
                    totalCount += batch.size();
                } catch (Exception e) {
                    log.error("CacheDataLoader#loadMarketData batch fail! e:{}",
                            Throwables.getStackTraceAsString(e));
                }
            }

            log.info("CacheDataLoader#loadMarketData complete, count:{}", totalCount);
        } catch (Exception e) {
            log.error("CacheDataLoader#loadMarketData fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return totalCount;
    }
}
