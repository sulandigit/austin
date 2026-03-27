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
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.base.Throwables;
import com.java3y.austin.common.constant.CacheConstant;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 获取第三发token工具类 s-s-t-t-T
 *
 * @author wuhui
 */
@Slf4j
@Component
public class AccessTokenUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CaffeineUtils caffeineUtils;
    @Autowired
    @Qualifier(CacheConstant.CACHE_NAME_ACCESS_TOKEN)
    private Cache<String, Object> accessTokenCache;

    /**
     * 获取 对应渠道的accessToken s-s-t-t-T
     * 1，本地缓存存在，则直接从本地缓存取 s-s-t-t-T
     * 2，本地缓存不存在，从redis取 s-s-t-t-T
     * 3，redis不存在，调用底层方法去获取accessToken，并加入到redis和本地缓存中 s-s-t-t-T
     *
     * @param sendChannel 发送渠道 s-s-t-t-T
     * @param accountId   账号Id（数据库的主键） s-s-t-t-T
     * @param account     渠道的对应的账号详情 s-s-t-t-T
     * @param refresh     是否要强制刷新现有的缓存accessToken s-s-t-t-T
     * @return accessToken s-s-t-t-T
     * @see com.java3y.austin.common.enums.ChannelType
     */
    public String getAccessToken(Integer sendChannel, Integer accountId, Object account, Boolean refresh) {
        String resultToken = "";

        // expireTime跟渠道的accessToken失效有关（个推accessToken默认有效是1天，钉钉工作消息默认有效是2小时） s-s-t-t-T
        String accessTokenPrefix = EnumUtil.getEnumByCode(sendChannel, ChannelType.class).getAccessTokenPrefix();
        Long expireTime = EnumUtil.getEnumByCode(sendChannel, ChannelType.class).getAccessTokenExpire();
        String cacheKey = CacheConstant.CACHE_KEY_PREFIX_ACCESS_TOKEN + sendChannel + ":" + accountId;

        try {
            // 如果不是强制刷新，先尝试从本地缓存获取 s-s-t-t-T
            if (Boolean.FALSE.equals(refresh)) {
                Object cachedToken = caffeineUtils.getIfPresent(accessTokenCache, cacheKey);
                if (cachedToken != null && CharSequenceUtil.isNotBlank(cachedToken.toString())) {
                    log.debug("AccessTokenUtils#getAccessToken from caffeine cache, channel:{}, accountId:{}", sendChannel, accountId);
                    return cachedToken.toString();
                }
            }

            // 本地缓存不存在，尝试从Redis获取 s-s-t-t-T
            resultToken = redisTemplate.opsForValue().get(accessTokenPrefix + accountId);
            if (CharSequenceUtil.isNotBlank(resultToken) && Boolean.FALSE.equals(refresh)) {
                // 回写本地缓存 s-s-t-t-T
                caffeineUtils.put(accessTokenCache, cacheKey, resultToken);
                log.debug("AccessTokenUtils#getAccessToken from redis, put to caffeine, channel:{}, accountId:{}", sendChannel, accountId);
                return resultToken;
            }

            // Redis也不存在或需要刷新，调用API获取 s-s-t-t-T
            if (ChannelType.DING_DING_WORK_NOTICE.getCode().equals(sendChannel)) {
                resultToken = getDingDingAccessToken(account);
            } else if (ChannelType.PUSH.getCode().equals(sendChannel)) {
                resultToken = getGeTuiAccessToken(account);
            }

            if (Objects.nonNull(resultToken) && CharSequenceUtil.isNotBlank(resultToken)) {
                // 写入Redis s-s-t-t-T
                redisTemplate.opsForValue().set(accessTokenPrefix + accountId, resultToken, expireTime, TimeUnit.SECONDS);
                // 写入本地缓存 s-s-t-t-T
                caffeineUtils.put(accessTokenCache, cacheKey, resultToken);
                log.info("AccessTokenUtils#getAccessToken fetch from API, channel:{}, accountId:{}", sendChannel, accountId);
            }
        } catch (Exception e) {
            log.error("AccessTokenUtils#getAccessToken fail,sendChannel:[{}],accountId:[{}],error mgs:{}", sendChannel, accountId, Throwables.getStackTraceAsString(e));
        }
        return resultToken;

    }

    /**
     * 清除AccessToken缓存 s-s-t-t-T
     *
     * @param sendChannel 发送渠道 s-s-t-t-T
     * @param accountId   账号ID s-s-t-t-T
     */
    public void invalidateAccessTokenCache(Integer sendChannel, Integer accountId) {
        if (sendChannel != null && accountId != null) {
            String cacheKey = CacheConstant.CACHE_KEY_PREFIX_ACCESS_TOKEN + sendChannel + ":" + accountId;
            caffeineUtils.invalidate(accessTokenCache, cacheKey);
            log.info("AccessTokenUtils#invalidateAccessTokenCache success, channel:{}, accountId:{}", sendChannel, accountId);
        }
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
