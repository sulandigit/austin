package com.java3y.austin.support.utils;


import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaRedisBetterConfigImpl;
import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.base.Throwables;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

/**
 * 获取账号信息工具类
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

    /**
     * 微信公众号服务实例缓存
     * 使用 Caffeine Cache 替代 ConcurrentHashMap
     * - 最大容量 500
     * - 访问后 1 小时过期
     * - 写入后 30 分钟刷新
     * - 启用弱值引用
     * - 启用统计功能
     */
    private LoadingCache<ChannelAccount, WxMpService> officialAccountServiceCache;

    /**
     * 微信小程序服务实例缓存
     * 配置同公众号缓存
     */
    private LoadingCache<ChannelAccount, WxMaService> miniProgramServiceCache;

    @PostConstruct
    private void init() {
        // 初始化公众号服务缓存
        officialAccountServiceCache = CaffeineUtils.buildObjectCache(account -> {
            try {
                WeChatOfficialAccount officialAccount = JSON.parseObject(
                        account.getAccountConfig(), WeChatOfficialAccount.class);
                return initOfficialAccountService(officialAccount);
            } catch (Exception e) {
                log.error("AccountUtils init official account service fail! account:{}, e:{}",
                        account.getId(), Throwables.getStackTraceAsString(e));
                return null;
            }
        });

        // 初始化小程序服务缓存
        miniProgramServiceCache = CaffeineUtils.buildObjectCache(account -> {
            try {
                WeChatMiniProgramAccount miniProgramAccount = JSON.parseObject(
                        account.getAccountConfig(), WeChatMiniProgramAccount.class);
                return initMiniProgramService(miniProgramAccount);
            } catch (Exception e) {
                log.error("AccountUtils init mini program service fail! account:{}, e:{}",
                        account.getId(), Throwables.getStackTraceAsString(e));
                return null;
            }
        });

        CaffeineUtils.logCacheInit("OfficialAccountService", 500,
                "expireAfterAccess=1h, refreshAfterWrite=30min, weakValues=true");
        CaffeineUtils.logCacheInit("MiniProgramService", 500,
                "expireAfterAccess=1h, refreshAfterWrite=30min, weakValues=true");
    }

    @Bean
    public RedisTemplateWxRedisOps redisTemplateWxRedisOps() {
        return new RedisTemplateWxRedisOps(this.redisTemplate);
    }

    /**
     * 微信小程序：返回 WxMaService
     * 微信服务号：返回 WxMpService
     * 其他渠道：返回XXXAccount账号对象
     *
     * @param sendAccountId
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getAccountById(Integer sendAccountId, Class<T> clazz) {
        try {
            Optional<ChannelAccount> optionalChannelAccount = channelAccountDao.findById(Long.valueOf(sendAccountId));
            if (optionalChannelAccount.isPresent()) {
                ChannelAccount channelAccount = optionalChannelAccount.get();
                if (clazz.equals(WxMaService.class)) {
                    // 使用 Caffeine 缓存自动加载
                    return (T) miniProgramServiceCache.get(channelAccount);
                } else if (clazz.equals(WxMpService.class)) {
                    // 使用 Caffeine 缓存自动加载
                    return (T) officialAccountServiceCache.get(channelAccount);
                } else {
                    return JSON.parseObject(channelAccount.getAccountConfig(), clazz);
                }
            }
        } catch (Exception e) {
            log.error("AccountUtils#getAccount fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    /**
     * 通过脚本名 匹配到对应的短信账号
     *
     * @param scriptName 脚本名
     * @param clazz
     * @param <T>
     * @return
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
     * 初始化微信服务号
     * access_token 用redis存储
     *
     * @return
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
     * 初始化微信小程序
     * access_token 用redis存储
     *
     * @return
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

    /**
     * 刷新账号缓存
     *
     * @param sendAccountId 账号ID
     */
    public void refreshAccountCache(Integer sendAccountId) {
        try {
            Optional<ChannelAccount> optionalChannelAccount = channelAccountDao.findById(Long.valueOf(sendAccountId));
            if (optionalChannelAccount.isPresent()) {
                ChannelAccount channelAccount = optionalChannelAccount.get();
                officialAccountServiceCache.invalidate(channelAccount);
                miniProgramServiceCache.invalidate(channelAccount);
                log.info("AccountUtils refresh account cache! accountId:{}", sendAccountId);
            }
        } catch (Exception e) {
            log.error("AccountUtils refresh account cache fail! accountId:{}, e:{}",
                    sendAccountId, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    public String getCacheStats() {
        return "OfficialAccount: " + CaffeineUtils.formatCacheStats("OfficialAccountService", officialAccountServiceCache) +
                "\nMiniProgram: " + CaffeineUtils.formatCacheStats("MiniProgramService", miniProgramServiceCache);
    }


}
