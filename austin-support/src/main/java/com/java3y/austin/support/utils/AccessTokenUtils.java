package com.java3y.austin.support.utils;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.google.common.base.Throwables;
import com.java3y.austin.common.constant.CommonConstant;
import com.java3y.austin.common.constant.SendChanelUrlConstant;
import com.java3y.austin.common.dto.account.DingDingWorkNoticeAccount;
import com.java3y.austin.common.dto.account.GeTuiAccount;
import com.java3y.austin.common.enums.ChannelType;
import com.java3y.austin.common.enums.EnumUtil;
import com.java3y.austin.support.dto.GeTuiTokenResultDTO;
import com.java3y.austin.support.dto.QueryTokenParamDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 获取第三发token工具类
 *
 * @author wuhui
 */
@Slf4j
@Component
public class AccessTokenUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private DistributedLockUtil distributedLockUtil;
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 空值缓存过期时间（秒）- 5分钟
     */
    private static final Long NULL_CACHE_EXPIRE_TIME = 300L;

    /**
     * 空值标记
     */
    private static final String NULL_VALUE_MARKER = "NULL";

    /**
     * 获取 对应渠道的accessToken
     * 1，redis存在，则直接从 redis 取
     * 2，redis不存在，使用分布式锁，调用底层方法去获取 accessToken，并加入到 redis 中
     *
     * @param sendChannel
     * @param accountId   账号Id（数据库的主键）
     * @param account     渠道的对应的账号详情
     * @param refresh     是否要强制刷新现有的缓存accessToken
     * @return
     * @see com.java3y.austin.common.enums.ChannelType
     */
    public String getAccessToken(Integer sendChannel, Integer accountId, Object account, Boolean refresh) {
        String resultToken = "";

        // expireTime跟渠道的accessToken失效有关（个推accessToken默认有效是1天，钉钉工作消息默认有效是2小时）
        String accessTokenPrefix = EnumUtil.getEnumByCode(sendChannel, ChannelType.class).getAccessTokenPrefix();
        Long expireTime = EnumUtil.getEnumByCode(sendChannel, ChannelType.class).getAccessTokenExpire();
        String cacheKey = accessTokenPrefix + accountId;

        try {
            // 强制刷新，直接查询
            if (Boolean.TRUE.equals(refresh)) {
                return refreshAccessToken(sendChannel, accountId, account, cacheKey, expireTime);
            }

            // 1. 查询缓存
            resultToken = redisUtils.get(cacheKey);
            
            // 2. 命中空值缓存，直接返回空
            if (NULL_VALUE_MARKER.equals(resultToken)) {
                log.warn("AccessTokenUtils#getAccessToken hit null cache! sendChannel:{}, accountId:{}", 
                        sendChannel, accountId);
                return "";
            }
            
            // 3. 命中正常缓存，直接返回
            if (CharSequenceUtil.isNotBlank(resultToken)) {
                return resultToken;
            }

            // 4. 缓存未命中，使用分布式锁防止击穿
            String lockKey = "access_token:" + sendChannel + ":" + accountId;
            resultToken = distributedLockUtil.executeWithLock(lockKey, () -> {
                // 双重检查：获取锁后再次查询缓存
                String cachedToken = redisUtils.get(cacheKey);
                if (CharSequenceUtil.isNotBlank(cachedToken) && !NULL_VALUE_MARKER.equals(cachedToken)) {
                    return cachedToken;
                }

                // 查询第三方接口获取 token
                String token = fetchAccessTokenFromProvider(sendChannel, account);
                
                if (Objects.nonNull(token) && CharSequenceUtil.isNotBlank(token)) {
                    // 写入缓存（带随机过期时间）
                    redisUtils.setWithRandomExpire(cacheKey, token, expireTime);
                } else {
                    // 空值缓存，防止穿透
                    redisTemplate.opsForValue().set(cacheKey, NULL_VALUE_MARKER, NULL_CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                    log.warn("AccessTokenUtils#getAccessToken fetch token fail, cache null! sendChannel:{}, accountId:{}", 
                            sendChannel, accountId);
                }
                return token;
            });

            // 如果获取锁失败，再次尝试查询缓存
            if (resultToken == null) {
                resultToken = redisUtils.get(cacheKey);
                if (NULL_VALUE_MARKER.equals(resultToken)) {
                    resultToken = "";
                }
            }
        } catch (Exception e) {
            log.error("AccessTokenUtils#getAccessToken fail,sendChannel:[{}],accountId:[{}],error mgs:{}", 
                    sendChannel, accountId, Throwables.getStackTraceAsString(e));
        }
        return resultToken != null ? resultToken : "";
    }

    /**
     * 刷新 access token
     */
    private String refreshAccessToken(Integer sendChannel, Integer accountId, Object account, 
                                       String cacheKey, Long expireTime) {
        // 删除旧缓存
        redisUtils.delete(cacheKey);
        
        // 查询新 token
        String token = fetchAccessTokenFromProvider(sendChannel, account);
        
        if (Objects.nonNull(token) && CharSequenceUtil.isNotBlank(token)) {
            redisUtils.setWithRandomExpire(cacheKey, token, expireTime);
        }
        return token;
    }

    /**
     * 从第三方提供商获取 access token
     */
    private String fetchAccessTokenFromProvider(Integer sendChannel, Object account) {
        if (ChannelType.DING_DING_WORK_NOTICE.getCode().equals(sendChannel)) {
            return getDingDingAccessToken(account);
        } else if (ChannelType.PUSH.getCode().equals(sendChannel)) {
            return getGeTuiAccessToken(account);
        }
        return "";
    }

    /**
     * 获取钉钉 access_token
     *
     * @param account 钉钉工作消息 账号信息
     * @return 钉钉 access_token
     */
    private String getDingDingAccessToken(Object account) {
        String accessToken = "";
        try {
            DingDingWorkNoticeAccount dingWorkNoticeAccount = (DingDingWorkNoticeAccount) account;
            DingTalkClient client = new DefaultDingTalkClient(SendChanelUrlConstant.DING_DING_TOKEN_URL);
            OapiGettokenRequest req = new OapiGettokenRequest();
            req.setAppkey(dingWorkNoticeAccount.getAppKey());
            req.setAppsecret(dingWorkNoticeAccount.getAppSecret());
            req.setHttpMethod(CommonConstant.REQUEST_METHOD_GET);
            OapiGettokenResponse rsp = client.execute(req);
            accessToken = rsp.getAccessToken();
        } catch (Exception e) {
            log.error("AccessTokenUtils#getDingDingAccessToken fail:{}", Throwables.getStackTraceAsString(e));
        }
        return accessToken;
    }

    /**
     * 获取个推的 access_token
     *
     * @param account 创建个推账号时的元信息
     * @return 个推的 access_token
     */
    private String getGeTuiAccessToken(Object account) {
        String accessToken = "";
        try {
            GeTuiAccount geTuiAccount = (GeTuiAccount) account;
            String url = SendChanelUrlConstant.GE_TUI_BASE_URL + geTuiAccount.getAppId() + SendChanelUrlConstant.GE_TUI_AUTH;
            String time = String.valueOf(System.currentTimeMillis());
            String digest = SecureUtil.sha256().digestHex(geTuiAccount.getAppKey() + time + geTuiAccount.getMasterSecret());
            QueryTokenParamDTO param = QueryTokenParamDTO.builder()
                    .timestamp(time)
                    .appKey(geTuiAccount.getAppKey())
                    .sign(digest).build();

            String body = HttpRequest.post(url).header(Header.CONTENT_TYPE.getValue(), ContentType.JSON.getValue())
                    .body(JSON.toJSONString(param))
                    .timeout(2000)
                    .execute().body();
            GeTuiTokenResultDTO geTuiTokenResultDTO = JSON.parseObject(body, GeTuiTokenResultDTO.class);
            if (geTuiTokenResultDTO.getCode().equals(0)) {
                accessToken = geTuiTokenResultDTO.getData().getToken();
            }
        } catch (Exception e) {
            log.error("AccessTokenUtils#getGeTuiAccessToken fail:{}", Throwables.getStackTraceAsString(e));
        }
        return accessToken;
    }
}
