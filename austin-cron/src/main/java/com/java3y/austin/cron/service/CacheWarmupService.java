package com.java3y.austin.cron.service;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.java3y.austin.support.dao.ChannelAccountDao;
import com.java3y.austin.support.dao.MessageTemplateDao;
import com.java3y.austin.support.domain.ChannelAccount;
import com.java3y.austin.support.domain.MessageTemplate;
import com.java3y.austin.support.utils.BloomFilterManager;
import com.java3y.austin.support.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 缓存预热服务
 * 在系统启动时或定时任务触发时，预加载热点数据到缓存
 *
 * @author 3y
 */
@Slf4j
@Service
public class CacheWarmupService {

    @Autowired
    private MessageTemplateDao messageTemplateDao;

    @Autowired
    private ChannelAccountDao channelAccountDao;

    @Autowired
    private BloomFilterManager bloomFilterManager;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 模板缓存过期时间（秒）- 15分钟
     */
    private static final Long TEMPLATE_CACHE_EXPIRE_TIME = 900L;

    /**
     * 账号缓存过期时间（秒）- 30分钟
     */
    private static final Long ACCOUNT_CACHE_EXPIRE_TIME = 1800L;

    /**
     * 模板缓存 Key 前缀
     */
    private static final String TEMPLATE_CACHE_PREFIX = "tpl:";

    /**
     * 账号缓存 Key 前缀
     */
    private static final String ACCOUNT_CACHE_PREFIX = "account:";

    /**
     * 系统启动时预热缓存
     */
    @PostConstruct
    public void init() {
        log.info("CacheWarmupService init start...");
        try {
            // 延迟5秒执行，等待其他组件初始化完成
            Thread.sleep(5000);
            warmupAllCache();
        } catch (Exception e) {
            log.error("CacheWarmupService init fail! e:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 预热所有缓存
     */
    public void warmupAllCache() {
        long startTime = System.currentTimeMillis();
        log.info("CacheWarmupService#warmupAllCache start...");

        try {
            // 1. 预热消息模板布隆过滤器和缓存
            warmupMessageTemplateCache();

            // 2. 预热渠道账号布隆过滤器和缓存
            warmupChannelAccountCache();

            long endTime = System.currentTimeMillis();
            log.info("CacheWarmupService#warmupAllCache success! cost:{}ms", (endTime - startTime));
        } catch (Exception e) {
            log.error("CacheWarmupService#warmupAllCache fail! e:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 预热消息模板缓存
     */
    public void warmupMessageTemplateCache() {
        try {
            log.info("CacheWarmupService#warmupMessageTemplateCache start...");

            // 1. 查询所有未删除的模板
            List<MessageTemplate> templateList = messageTemplateDao.findAllByIsDeletedEqualsOrderByUpdatedDesc(0, null);

            if (templateList == null || templateList.isEmpty()) {
                log.warn("CacheWarmupService#warmupMessageTemplateCache no data found!");
                return;
            }

            // 2. 重建布隆过滤器
            bloomFilterManager.delete(BloomFilterManager.getTemplateFilter());
            List<String> templateIds = templateList.stream()
                    .map(t -> String.valueOf(t.getId()))
                    .collect(Collectors.toList());
            bloomFilterManager.batchAdd(BloomFilterManager.getTemplateFilter(), templateIds);

            // 3. 预热缓存（只预热最近更新的前100个）
            int warmupCount = Math.min(templateList.size(), 100);
            for (int i = 0; i < warmupCount; i++) {
                MessageTemplate template = templateList.get(i);
                String cacheKey = TEMPLATE_CACHE_PREFIX + template.getId();
                redisUtils.setWithRandomExpire(cacheKey, JSON.toJSONString(template), TEMPLATE_CACHE_EXPIRE_TIME);
            }

            log.info("CacheWarmupService#warmupMessageTemplateCache success! bloomCount:{}, cacheCount:{}", 
                    templateIds.size(), warmupCount);
        } catch (Exception e) {
            log.error("CacheWarmupService#warmupMessageTemplateCache fail! e:{}", 
                    Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 预热渠道账号缓存
     */
    public void warmupChannelAccountCache() {
        try {
            log.info("CacheWarmupService#warmupChannelAccountCache start...");

            // 1. 查询所有未删除的账号
            List<ChannelAccount> accountList = channelAccountDao.findAllByIsDeletedEquals(0);

            if (accountList == null || accountList.isEmpty()) {
                log.warn("CacheWarmupService#warmupChannelAccountCache no data found!");
                return;
            }

            // 2. 重建布隆过滤器
            bloomFilterManager.delete(BloomFilterManager.getAccountFilter());
            List<String> accountIds = accountList.stream()
                    .map(a -> String.valueOf(a.getId()))
                    .collect(Collectors.toList());
            bloomFilterManager.batchAdd(BloomFilterManager.getAccountFilter(), accountIds);

            // 3. 预热缓存（预热所有账号，因为数量不会太多）
            for (ChannelAccount account : accountList) {
                String cacheKey = ACCOUNT_CACHE_PREFIX + account.getId();
                redisUtils.setWithRandomExpire(cacheKey, JSON.toJSONString(account), ACCOUNT_CACHE_EXPIRE_TIME);
            }

            log.info("CacheWarmupService#warmupChannelAccountCache success! bloomCount:{}, cacheCount:{}", 
                    accountIds.size(), accountList.size());
        } catch (Exception e) {
            log.error("CacheWarmupService#warmupChannelAccountCache fail! e:{}", 
                    Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 增量添加模板到布隆过滤器
     *
     * @param templateId 模板ID
     */
    public void addTemplateToBloomFilter(Long templateId) {
        try {
            bloomFilterManager.add(BloomFilterManager.getTemplateFilter(), String.valueOf(templateId));
            log.info("CacheWarmupService#addTemplateToBloomFilter success! templateId:{}", templateId);
        } catch (Exception e) {
            log.error("CacheWarmupService#addTemplateToBloomFilter fail! templateId:{}, e:{}", 
                    templateId, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 增量添加账号到布隆过滤器
     *
     * @param accountId 账号ID
     */
    public void addAccountToBloomFilter(Long accountId) {
        try {
            bloomFilterManager.add(BloomFilterManager.getAccountFilter(), String.valueOf(accountId));
            log.info("CacheWarmupService#addAccountToBloomFilter success! accountId:{}", accountId);
        } catch (Exception e) {
            log.error("CacheWarmupService#addAccountToBloomFilter fail! accountId:{}, e:{}", 
                    accountId, Throwables.getStackTraceAsString(e));
        }
    }
}
