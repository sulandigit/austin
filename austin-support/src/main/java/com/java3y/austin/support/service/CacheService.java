package com.java3y.austin.support.service;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.java3y.austin.common.constant.CommonConstant;
import com.java3y.austin.support.constans.CacheConstant;
import com.java3y.austin.support.dao.ChannelAccountDao;
import com.java3y.austin.support.dao.MessageTemplateDao;
import com.java3y.austin.support.dao.TemplateMarketDao;
import com.java3y.austin.support.domain.ChannelAccount;
import com.java3y.austin.support.domain.MessageTemplate;
import com.java3y.austin.support.domain.TemplateMarket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Cache service with penetration protection s-s-t-t-T
 *
 * @author austin
 * @date 2025/01/06
 */
@Slf4j
@Service
public class CacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BloomFilterManager bloomFilterManager;

    @Autowired
    private MessageTemplateDao messageTemplateDao;

    @Autowired
    private ChannelAccountDao channelAccountDao;

    @Autowired
    private TemplateMarketDao templateMarketDao;

    /**
     * Get message template with cache and bloom filter protection s-s-t-t-T
     *
     * @param templateId template ID
     * @return MessageTemplate or null
     */
    public MessageTemplate getMessageTemplate(Long templateId) {
        if (templateId == null) {
            return null;
        }

        try {
            // Step 1: Check bloom filter s-s-t-t-T
            if (!bloomFilterManager.mightContainTemplate(templateId)) {
                log.debug("CacheService#getMessageTemplate bloom filter reject, templateId:{}", templateId);
                return null;
            }

            // Step 2: Check cache s-s-t-t-T
            String cacheKey = CacheConstant.TEMPLATE_INFO_PREFIX + templateId;
            String cacheValue = redisTemplate.opsForValue().get(cacheKey);
            if (cacheValue != null) {
                // Check if it's a null value cache s-s-t-t-T
                if (CacheConstant.NULL_VALUE.equals(cacheValue)) {
                    log.debug("CacheService#getMessageTemplate null cache hit, templateId:{}", templateId);
                    return null;
                }
                log.debug("CacheService#getMessageTemplate cache hit, templateId:{}", templateId);
                return JSON.parseObject(cacheValue, MessageTemplate.class);
            }

            // Step 3: Check null value cache s-s-t-t-T
            String nullKey = CacheConstant.TEMPLATE_NULL_PREFIX + templateId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(nullKey))) {
                log.debug("CacheService#getMessageTemplate null value cache hit, templateId:{}", templateId);
                return null;
            }

            // Step 4: Query database s-s-t-t-T
            Optional<MessageTemplate> optional = messageTemplateDao.findById(templateId);
            if (optional.isPresent() && Objects.equals(CommonConstant.FALSE, optional.get().getIsDeleted())) {
                MessageTemplate template = optional.get();
                // Write to cache s-s-t-t-T
                redisTemplate.opsForValue().set(
                        cacheKey,
                        JSON.toJSONString(template),
                        CacheConstant.TEMPLATE_CACHE_EXPIRE,
                        TimeUnit.SECONDS
                );
                // Add to bloom filter s-s-t-t-T
                bloomFilterManager.addTemplate(templateId);
                log.debug("CacheService#getMessageTemplate db hit and cached, templateId:{}", templateId);
                return template;
            } else {
                // Write null value cache s-s-t-t-T
                redisTemplate.opsForValue().set(
                        nullKey,
                        CacheConstant.NULL_VALUE,
                        CacheConstant.NULL_CACHE_EXPIRE,
                        TimeUnit.SECONDS
                );
                log.debug("CacheService#getMessageTemplate db miss, null cache set, templateId:{}", templateId);
                return null;
            }
        } catch (Exception e) {
            log.error("CacheService#getMessageTemplate fail! templateId:{}, e:{}",
                    templateId, Throwables.getStackTraceAsString(e));
            // Fallback to database query on error s-s-t-t-T
            Optional<MessageTemplate> optional = messageTemplateDao.findById(templateId);
            return optional.filter(template -> Objects.equals(CommonConstant.FALSE, template.getIsDeleted())).orElse(null);
        }
    }

    /**
     * Get channel account with cache and bloom filter protection s-s-t-t-T
     *
     * @param accountId account ID
     * @return ChannelAccount or null
     */
    public ChannelAccount getChannelAccount(Long accountId) {
        if (accountId == null) {
            return null;
        }

        try {
            // Step 1: Check bloom filter s-s-t-t-T
            if (!bloomFilterManager.mightContainAccount(accountId)) {
                log.debug("CacheService#getChannelAccount bloom filter reject, accountId:{}", accountId);
                return null;
            }

            // Step 2: Check cache s-s-t-t-T
            String cacheKey = CacheConstant.ACCOUNT_INFO_PREFIX + accountId;
            String cacheValue = redisTemplate.opsForValue().get(cacheKey);
            if (cacheValue != null) {
                // Check if it's a null value cache s-s-t-t-T
                if (CacheConstant.NULL_VALUE.equals(cacheValue)) {
                    log.debug("CacheService#getChannelAccount null cache hit, accountId:{}", accountId);
                    return null;
                }
                log.debug("CacheService#getChannelAccount cache hit, accountId:{}", accountId);
                return JSON.parseObject(cacheValue, ChannelAccount.class);
            }

            // Step 3: Check null value cache s-s-t-t-T
            String nullKey = CacheConstant.ACCOUNT_NULL_PREFIX + accountId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(nullKey))) {
                log.debug("CacheService#getChannelAccount null value cache hit, accountId:{}", accountId);
                return null;
            }

            // Step 4: Query database s-s-t-t-T
            Optional<ChannelAccount> optional = channelAccountDao.findById(accountId);
            if (optional.isPresent() && Objects.equals(CommonConstant.FALSE, optional.get().getIsDeleted())) {
                ChannelAccount account = optional.get();
                // Write to cache s-s-t-t-T
                redisTemplate.opsForValue().set(
                        cacheKey,
                        JSON.toJSONString(account),
                        CacheConstant.ACCOUNT_CACHE_EXPIRE,
                        TimeUnit.SECONDS
                );
                // Add to bloom filter s-s-t-t-T
                bloomFilterManager.addAccount(accountId);
                log.debug("CacheService#getChannelAccount db hit and cached, accountId:{}", accountId);
                return account;
            } else {
                // Write null value cache s-s-t-t-T
                redisTemplate.opsForValue().set(
                        nullKey,
                        CacheConstant.NULL_VALUE,
                        CacheConstant.NULL_CACHE_EXPIRE,
                        TimeUnit.SECONDS
                );
                log.debug("CacheService#getChannelAccount db miss, null cache set, accountId:{}", accountId);
                return null;
            }
        } catch (Exception e) {
            log.error("CacheService#getChannelAccount fail! accountId:{}, e:{}",
                    accountId, Throwables.getStackTraceAsString(e));
            // Fallback to database query on error s-s-t-t-T
            Optional<ChannelAccount> optional = channelAccountDao.findById(accountId);
            return optional.filter(account -> Objects.equals(CommonConstant.FALSE, account.getIsDeleted())).orElse(null);
        }
    }

    /**
     * Get template market with cache and bloom filter protection s-s-t-t-T
     *
     * @param marketId market template ID
     * @return TemplateMarket or null
     */
    public TemplateMarket getTemplateMarket(Long marketId) {
        if (marketId == null) {
            return null;
        }

        try {
            // Step 1: Check bloom filter s-s-t-t-T
            if (!bloomFilterManager.mightContainMarket(marketId)) {
                log.debug("CacheService#getTemplateMarket bloom filter reject, marketId:{}", marketId);
                return null;
            }

            // Step 2: Check cache s-s-t-t-T
            String cacheKey = CacheConstant.MARKET_TEMPLATE_PREFIX + marketId;
            String cacheValue = redisTemplate.opsForValue().get(cacheKey);
            if (cacheValue != null) {
                // Check if it's a null value cache s-s-t-t-T
                if (CacheConstant.NULL_VALUE.equals(cacheValue)) {
                    log.debug("CacheService#getTemplateMarket null cache hit, marketId:{}", marketId);
                    return null;
                }
                log.debug("CacheService#getTemplateMarket cache hit, marketId:{}", marketId);
                return JSON.parseObject(cacheValue, TemplateMarket.class);
            }

            // Step 3: Check null value cache s-s-t-t-T
            String nullKey = CacheConstant.MARKET_NULL_PREFIX + marketId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(nullKey))) {
                log.debug("CacheService#getTemplateMarket null value cache hit, marketId:{}", marketId);
                return null;
            }

            // Step 4: Query database s-s-t-t-T
            Optional<TemplateMarket> optional = templateMarketDao.findById(marketId);
            if (optional.isPresent()) {
                TemplateMarket market = optional.get();
                // Write to cache s-s-t-t-T
                redisTemplate.opsForValue().set(
                        cacheKey,
                        JSON.toJSONString(market),
                        CacheConstant.MARKET_CACHE_EXPIRE,
                        TimeUnit.SECONDS
                );
                // Add to bloom filter s-s-t-t-T
                bloomFilterManager.addMarket(marketId);
                log.debug("CacheService#getTemplateMarket db hit and cached, marketId:{}", marketId);
                return market;
            } else {
                // Write null value cache s-s-t-t-T
                redisTemplate.opsForValue().set(
                        nullKey,
                        CacheConstant.NULL_VALUE,
                        CacheConstant.NULL_CACHE_EXPIRE,
                        TimeUnit.SECONDS
                );
                log.debug("CacheService#getTemplateMarket db miss, null cache set, marketId:{}", marketId);
                return null;
            }
        } catch (Exception e) {
            log.error("CacheService#getTemplateMarket fail! marketId:{}, e:{}",
                    marketId, Throwables.getStackTraceAsString(e));
            // Fallback to database query on error s-s-t-t-T
            return templateMarketDao.findById(marketId).orElse(null);
        }
    }

    /**
     * Update message template cache s-s-t-t-T
     *
     * @param template message template
     */
    public void updateMessageTemplateCache(MessageTemplate template) {
        if (template == null || template.getId() == null) {
            return;
        }

        try {
            String cacheKey = CacheConstant.TEMPLATE_INFO_PREFIX + template.getId();
            String nullKey = CacheConstant.TEMPLATE_NULL_PREFIX + template.getId();

            // Delete null value cache s-s-t-t-T
            redisTemplate.delete(nullKey);

            // Update cache s-s-t-t-T
            redisTemplate.opsForValue().set(
                    cacheKey,
                    JSON.toJSONString(template),
                    CacheConstant.TEMPLATE_CACHE_EXPIRE,
                    TimeUnit.SECONDS
            );

            // Add to bloom filter s-s-t-t-T
            bloomFilterManager.addTemplate(template.getId());

            log.info("CacheService#updateMessageTemplateCache success, templateId:{}", template.getId());
        } catch (Exception e) {
            log.error("CacheService#updateMessageTemplateCache fail! templateId:{}, e:{}",
                    template.getId(), Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * Delete message template cache s-s-t-t-T
     *
     * @param templateId template ID
     */
    public void deleteMessageTemplateCache(Long templateId) {
        if (templateId == null) {
            return;
        }

        try {
            String cacheKey = CacheConstant.TEMPLATE_INFO_PREFIX + templateId;
            String nullKey = CacheConstant.TEMPLATE_NULL_PREFIX + templateId;

            // Delete cache s-s-t-t-T
            redisTemplate.delete(cacheKey);

            // Set null value cache s-s-t-t-T
            redisTemplate.opsForValue().set(
                    nullKey,
                    CacheConstant.NULL_VALUE,
                    CacheConstant.NULL_CACHE_EXPIRE,
                    TimeUnit.SECONDS
            );

            log.info("CacheService#deleteMessageTemplateCache success, templateId:{}", templateId);
        } catch (Exception e) {
            log.error("CacheService#deleteMessageTemplateCache fail! templateId:{}, e:{}",
                    templateId, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * Update channel account cache s-s-t-t-T
     *
     * @param account channel account
     */
    public void updateChannelAccountCache(ChannelAccount account) {
        if (account == null || account.getId() == null) {
            return;
        }

        try {
            String cacheKey = CacheConstant.ACCOUNT_INFO_PREFIX + account.getId();
            String nullKey = CacheConstant.ACCOUNT_NULL_PREFIX + account.getId();

            // Delete null value cache s-s-t-t-T
            redisTemplate.delete(nullKey);

            // Update cache s-s-t-t-T
            redisTemplate.opsForValue().set(
                    cacheKey,
                    JSON.toJSONString(account),
                    CacheConstant.ACCOUNT_CACHE_EXPIRE,
                    TimeUnit.SECONDS
            );

            // Add to bloom filter s-s-t-t-T
            bloomFilterManager.addAccount(account.getId());

            log.info("CacheService#updateChannelAccountCache success, accountId:{}", account.getId());
        } catch (Exception e) {
            log.error("CacheService#updateChannelAccountCache fail! accountId:{}, e:{}",
                    account.getId(), Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * Delete channel account cache s-s-t-t-T
     *
     * @param accountId account ID
     */
    public void deleteChannelAccountCache(Long accountId) {
        if (accountId == null) {
            return;
        }

        try {
            String cacheKey = CacheConstant.ACCOUNT_INFO_PREFIX + accountId;
            String nullKey = CacheConstant.ACCOUNT_NULL_PREFIX + accountId;

            // Delete cache s-s-t-t-T
            redisTemplate.delete(cacheKey);

            // Set null value cache s-s-t-t-T
            redisTemplate.opsForValue().set(
                    nullKey,
                    CacheConstant.NULL_VALUE,
                    CacheConstant.NULL_CACHE_EXPIRE,
                    TimeUnit.SECONDS
            );

            log.info("CacheService#deleteChannelAccountCache success, accountId:{}", accountId);
        } catch (Exception e) {
            log.error("CacheService#deleteChannelAccountCache fail! accountId:{}, e:{}",
                    accountId, Throwables.getStackTraceAsString(e));
        }
    }
}
