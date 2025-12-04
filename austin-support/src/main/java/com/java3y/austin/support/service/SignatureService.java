package com.java3y.austin.support.service;

import cn.hutool.core.text.CharSequenceUtil;
import com.google.common.base.Throwables;
import com.java3y.austin.common.constant.SignatureConstant;
import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.support.dao.AppInfoDao;
import com.java3y.austin.support.domain.AppInfo;
import com.java3y.austin.support.utils.SignatureUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 签名验证服务
 * 实现签名验证的核心逻辑
 *
 * @author austin
 */
@Slf4j
@Service
public class SignatureService {

    @Autowired
    private AppInfoDao appInfoDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 验证签名
     *
     * @param appId     应用ID
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @param signature 签名
     * @param method    HTTP方法
     * @param path      请求路径
     * @param body      请求体
     * @param version   签名版本
     * @return 验证结果，成功返回null，失败返回错误码枚举
     */
    public RespStatusEnum verifySignature(String appId, String timestamp, String nonce,
                                          String signature, String method, String path,
                                          String body, String version) {
        try {
            // 1. 校验必填参数
            if (CharSequenceUtil.isBlank(appId) || CharSequenceUtil.isBlank(timestamp) ||
                    CharSequenceUtil.isBlank(nonce) || CharSequenceUtil.isBlank(signature)) {
                log.warn("签名参数缺失: appId={}, timestamp={}, nonce={}, signature={}",
                        appId, timestamp, nonce, signature != null ? "***" : null);
                return RespStatusEnum.SIGNATURE_PARAM_MISSING;
            }

            // 2. 校验参数格式
            if (!SignatureUtils.isValidNonce(nonce)) {
                log.warn("nonce格式错误: {}", nonce);
                return RespStatusEnum.SIGNATURE_PARAM_INVALID;
            }

            long timestampLong;
            try {
                timestampLong = Long.parseLong(timestamp);
            } catch (NumberFormatException e) {
                log.warn("timestamp格式错误: {}", timestamp);
                return RespStatusEnum.SIGNATURE_PARAM_INVALID;
            }

            // 3. 校验时间窗口
            if (!SignatureUtils.isValidTimestamp(timestampLong)) {
                log.warn("请求已超时: timestamp={}, current={}", timestampLong, System.currentTimeMillis() / 1000);
                return RespStatusEnum.SIGNATURE_TIMESTAMP_EXPIRED;
            }

            // 4. 防重放校验
            if (isReplayAttack(appId, nonce)) {
                log.warn("检测到重放攻击: appId={}, nonce={}", appId, nonce);
                return RespStatusEnum.SIGNATURE_REPLAY_ATTACK;
            }

            // 5. 加载应用信息
            Optional<AppInfo> appInfoOpt = appInfoDao.findByAppIdAndStatusAndIsDeleted(appId, 1, 0);
            if (!appInfoOpt.isPresent()) {
                log.warn("应用不存在或已被禁用: appId={}", appId);
                return RespStatusEnum.SIGNATURE_APP_NOT_FOUND;
            }

            AppInfo appInfo = appInfoOpt.get();

            // 6. 校验签名版本
            String signVersion = CharSequenceUtil.isBlank(version) ? 
                    SignatureConstant.DEFAULT_SIGN_VERSION : version;
            if (!signVersion.equals(appInfo.getSignVersion())) {
                log.warn("签名版本不匹配: request={}, expected={}", signVersion, appInfo.getSignVersion());
                return RespStatusEnum.SIGNATURE_VERSION_NOT_SUPPORT;
            }

            // 7. 计算签名并比对
            String calculatedSignature = SignatureUtils.generateSignature(
                    appId, timestamp, nonce, method, path, body,
                    appInfo.getAppSecret(), signVersion
            );

            if (!calculatedSignature.equals(signature)) {
                log.warn("签名验证失败: appId={}, expected={}, actual={}",
                        appId, calculatedSignature, signature);
                return RespStatusEnum.SIGNATURE_VERIFY_FAILED;
            }

            // 8. 记录nonce，防止重放
            recordNonce(appId, nonce);

            log.info("签名验证成功: appId={}", appId);
            return null;

        } catch (Exception e) {
            log.error("签名验证异常: {}", Throwables.getStackTraceAsString(e));
            return RespStatusEnum.SERVICE_ERROR;
        }
    }

    /**
     * 检查是否为重放攻击
     *
     * @param appId 应用ID
     * @param nonce 随机字符串
     * @return true-重放攻击，false-正常请求
     */
    private boolean isReplayAttack(String appId, String nonce) {
        try {
            String key = SignatureConstant.REDIS_KEY_PREFIX_NONCE + appId + ":" + nonce;
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("检查重放攻击失败: {}", Throwables.getStackTraceAsString(e));
            // 出现异常时，为了安全起见，允许请求通过
            return false;
        }
    }

    /**
     * 记录nonce，防止重放攻击
     *
     * @param appId 应用ID
     * @param nonce 随机字符串
     */
    private void recordNonce(String appId, String nonce) {
        try {
            String key = SignatureConstant.REDIS_KEY_PREFIX_NONCE + appId + ":" + nonce;
            // 设置过期时间为时间窗口的2倍，确保覆盖整个有效期
            long expireSeconds = SignatureConstant.TIMESTAMP_WINDOW_SECONDS * 2;
            redisTemplate.opsForValue().set(key, "1", expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("记录nonce失败: {}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 检查应用是否需要强制启用签名验证
     *
     * @param appId 应用ID
     * @return true-需要强制验证，false-不需要
     */
    public boolean isSignatureRequired(String appId) {
        try {
            if (CharSequenceUtil.isBlank(appId)) {
                return false;
            }
            Optional<AppInfo> appInfoOpt = appInfoDao.findByAppIdAndIsDeleted(appId, 0);
            if (appInfoOpt.isPresent()) {
                AppInfo appInfo = appInfoOpt.get();
                return appInfo.getSignatureEnabled() != null && appInfo.getSignatureEnabled() == 1;
            }
            return false;
        } catch (Exception e) {
            log.error("检查签名是否必需失败: {}", Throwables.getStackTraceAsString(e));
            return false;
        }
    }

}
