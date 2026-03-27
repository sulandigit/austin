package com.java3y.austin.support.utils;


import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaRedisBetterConfigImpl;
import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.base.Throwables;
import com.java3y.austin.common.constant.CacheConstant;
import com.java3y.austin.common.constant.CommonConstant;
import com.java3y.austin.common.dto.account.WeChatMiniProgramAccount;
import com.java3y.austin.common.dto.account.WeChatOfficialAccount;
import com.java3y.austin.common.dto.account.sms.SmsAccount;
import com.java3y.austin.common.enums.ChannelType;
import com.java3y.austin.support.dao.ChannelAccountDao;
import com.java3y.austin.support.domain.ChannelAccount;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 获取账号信息工具类 s-s-t-t-T
 *
 * @author 3y
 */
@Slf4j
@Configuration
public class AccountUtils {

    @Autowired
    private ChannelAccountDao channelAccountDao;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CaffeineUtils caffeineUtils;
    @Autowired
    @Qualifier(CacheConstant.CACHE_NAME_CHANNEL_ACCOUNT)
    private Cache<String, Object> channelAccountCache;

    /**
     * 消息的小程序/微信服务号账号 s-s-t-t-T
     */
    private final ConcurrentMap<ChannelAccount, WxMpService> officialAccountServiceMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<ChannelAccount, WxMaService> miniProgramServiceMap = new ConcurrentHashMap<>();

    @Bean
    public RedisTemplateWxRedisOps redisTemplateWxRedisOps() {
        return new RedisTemplateWxRedisOps(this.redisTemplate);
    }

    /**
     * 微信小程序：返回 WxMaService s-s-t-t-T
     * 微信服务号：返回 WxMpService s-s-t-t-T
     * 其他渠道：返回XXXAccount账号对象 s-s-t-t-T
     *
     * @param sendAccountId 账号ID s-s-t-t-T
     * @param clazz         返回类型 s-s-t-t-T
     * @param <T>           泛型类型 s-s-t-t-T
     * @return 账号对象 s-s-t-t-T
     */
    @SuppressWarnings("unchecked")
    public <T> T getAccountById(Integer sendAccountId, Class<T> clazz) {
        String cacheKey = CacheConstant.CACHE_KEY_PREFIX_CHANNEL_ACCOUNT + sendAccountId + ":" + clazz.getSimpleName();
        
        try {
            // 先从本地缓存获取 s-s-t-t-T
            Object cachedAccount = caffeineUtils.getIfPresent(channelAccountCache, cacheKey);
            if (cachedAccount != null) {
                log.debug("AccountUtils#getAccountById from caffeine cache, accountId:{}", sendAccountId);
                return (T) cachedAccount;
            }
            
            // 从数据库加载 s-s-t-t-T
            Optional<ChannelAccount> optionalChannelAccount = channelAccountDao.findById(Long.valueOf(sendAccountId));
            if (optionalChannelAccount.isPresent()) {
                ChannelAccount channelAccount = optionalChannelAccount.get();
                T result;
                if (clazz.equals(WxMaService.class)) {
                    result = (T) ConcurrentHashMapUtils.computeIfAbsent(miniProgramServiceMap, channelAccount, account -> initMiniProgramService(JSON.parseObject(account.getAccountConfig(), WeChatMiniProgramAccount.class)));
                } else if (clazz.equals(WxMpService.class)) {
                    result = (T) ConcurrentHashMapUtils.computeIfAbsent(officialAccountServiceMap, channelAccount, account -> initOfficialAccountService(JSON.parseObject(account.getAccountConfig(), WeChatOfficialAccount.class)));
                } else {
                    result = JSON.parseObject(channelAccount.getAccountConfig(), clazz);
                }
                
                // 写入本地缓存 s-s-t-t-T
                if (result != null) {
                    caffeineUtils.put(channelAccountCache, cacheKey, result);
                    log.debug("AccountUtils#getAccountById put to caffeine cache, accountId:{}", sendAccountId);
                }
                return result;
            }
        } catch (Exception e) {
            log.error("AccountUtils#getAccount fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    /**
     * 清除账号缓存 s-s-t-t-T
     *
     * @param sendAccountId 账号ID s-s-t-t-T
     */
    public void invalidateAccountCache(Integer sendAccountId) {
        if (sendAccountId != null) {
            // 清除所有该账号相关的缓存（可能有多个类型） s-s-t-t-T
            String keyPrefix = CacheConstant.CACHE_KEY_PREFIX_CHANNEL_ACCOUNT + sendAccountId;
            // 由于Caffeine不支持按前缀删除，这里需要遍历所有可能的类型 s-s-t-t-T
            caffeineUtils.invalidate(channelAccountCache, keyPrefix + ":WxMaService");
            caffeineUtils.invalidate(channelAccountCache, keyPrefix + ":WxMpService");
            caffeineUtils.invalidate(channelAccountCache, keyPrefix + ":SmsAccount");
            caffeineUtils.invalidate(channelAccountCache, keyPrefix + ":GeTuiAccount");
            caffeineUtils.invalidate(channelAccountCache, keyPrefix + ":DingDingWorkNoticeAccount");
            caffeineUtils.invalidate(channelAccountCache, keyPrefix + ":EnterpriseWeChatAccount");
            caffeineUtils.invalidate(channelAccountCache, keyPrefix + ":AlipayMiniProgramAccount");
            log.info("AccountUtils#invalidateAccountCache success, accountId:{}", sendAccountId);
        }
    }

    /**
     * 通过脚本名 匹配到对应的短信账号 s-s-t-t-T
     *
     * @param scriptName 脚本名 s-s-t-t-T
     * @param clazz      返回类型 s-s-t-t-T
     * @param <T>        泛型类型 s-s-t-t-T
     * @return 短信账号对象 s-s-t-t-T
     */
    public <T> T getSmsAccountByScriptName(String scriptName, Class<T> clazz) {
        try {
            List<ChannelAccount> channelAccountList = channelAccountDao.findAllByIsDeletedEqualsAndSendChannelEquals(CommonConstant.FALSE, ChannelType.SMS.getCode());
            for (ChannelAccount channelAccount : channelAccountList) {
                try {
                    SmsAccount smsAccount = JSON.parseObject(channelAccount.getAccountConfig(), SmsAccount.class);
                    if (smsAccount.getScriptName().equals(scriptName)) {
                        return JSON.parseObject(channelAccount.getAccountConfig(), clazz);
                    }
                } catch (Exception e) {
                    log.error("AccountUtils#getSmsAccount parse fail! e:{},account:{}", Throwables.getStackTraceAsString(e), JSON.toJSONString(channelAccount));
                }
            }
        } catch (Exception e) {
            log.error("AccountUtils#getSmsAccount fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        log.error("AccountUtils#getSmsAccount not found!:{}", scriptName);
        return null;
    }

    /**
     * 初始化微信服务号 s-s-t-t-T
     * access_token 用redis存储 s-s-t-t-T
     *
     * @param officialAccount 服务号配置 s-s-t-t-T
     * @return WxMpService实例 s-s-t-t-T
     */
    public WxMpService initOfficialAccountService(WeChatOfficialAccount officialAccount) {
        WxMpService wxMpService = new WxMpServiceImpl();
        WxMpRedisConfigImpl config = new WxMpRedisConfigImpl(redisTemplateWxRedisOps(), ChannelType.OFFICIAL_ACCOUNT.getAccessTokenPrefix());
        config.setAppId(officialAccount.getAppId());
        config.setSecret(officialAccount.getSecret());
        config.setToken(officialAccount.getToken());
        config.useStableAccessToken(true);
        wxMpService.setWxMpConfigStorage(config);
        return wxMpService;
    }

    /**
     * 初始化微信小程序 s-s-t-t-T
     * access_token 用redis存储 s-s-t-t-T
     *
     * @param miniProgramAccount 小程序配置 s-s-t-t-T
     * @return WxMaService实例 s-s-t-t-T
     */
    private WxMaService initMiniProgramService(WeChatMiniProgramAccount miniProgramAccount) {
        WxMaService wxMaService = new WxMaServiceImpl();
        WxMaRedisBetterConfigImpl config = new WxMaRedisBetterConfigImpl(redisTemplateWxRedisOps(), ChannelType.MINI_PROGRAM.getAccessTokenPrefix());
        config.setAppid(miniProgramAccount.getAppId());
        config.setSecret(miniProgramAccount.getAppSecret());
        config.useStableAccessToken(true);
        wxMaService.setWxMaConfig(config);
        return wxMaService;
    }


}
