package com.java3y.austin.web.aspect;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.alibaba.fastjson.JSON;
import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.web.annotation.PreventReplay;
import com.java3y.austin.web.config.ReplayPreventionConfig;
import com.java3y.austin.web.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 防重放切面处理器
 * 拦截带有 @PreventReplay 注解的方法,验证请求签名和时间戳
 *
 * @author austin
 */
@Slf4j
@Aspect
@Component
public class ReplayPreventionAspect {

    @Autowired
    private ReplayPreventionConfig config;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(com.java3y.austin.web.annotation.PreventReplay) || @within(com.java3y.austin.web.annotation.PreventReplay)")
    public Object preventReplay(ProceedingJoinPoint joinPoint) throws Throwable {
        // 检查是否启用防重放
        if (!config.isEnabled()) {
            return joinPoint.proceed();
        }

        // 获取注解
        PreventReplay annotation = getAnnotation(joinPoint);
        if (annotation == null) {
            return joinPoint.proceed();
        }

        // 获取请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        // 1. 获取请求参数
        String timestamp = getParameter(request, annotation.timestampParamName());
        String sign = getParameter(request, annotation.signParamName());
        String nonce = getParameter(request, annotation.nonceParamName());

        // 2. 验证时间戳
        if (annotation.requireTimestamp()) {
            validateTimestamp(timestamp, annotation.timeWindow());
        }

        // 3. 生成请求唯一标识
        String requestKey = generateRequestKey(request, timestamp, nonce, sign);

        // 4. 检查请求是否已存在(防重放)
        if (isRequestExists(requestKey)) {
            logReplayAttempt(request, timestamp, sign);
            throw new CommonException(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), "请求已被处理，请勿重复提交");
        }

        // 5. 验证签名
        if (annotation.requireSign()) {
            validateSign(request, timestamp, nonce, sign, annotation);
        }

        // 6. 记录请求(防重放)
        recordRequest(requestKey, annotation.timeWindow());

        // 7. 执行原方法
        return joinPoint.proceed();
    }

    /**
     * 获取注解
     */
    private PreventReplay getAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        PreventReplay annotation = method.getAnnotation(PreventReplay.class);
        if (annotation == null) {
            annotation = joinPoint.getTarget().getClass().getAnnotation(PreventReplay.class);
        }
        return annotation;
    }

    /**
     * 获取请求参数(支持Header和Parameter)
     */
    private String getParameter(HttpServletRequest request, String paramName) {
        // 优先从Header获取
        String value = request.getHeader(paramName);
        if (CharSequenceUtil.isBlank(value)) {
            // 从请求参数获取
            value = request.getParameter(paramName);
        }
        return value;
    }

    /**
     * 验证时间戳
     */
    private void validateTimestamp(String timestampStr, long timeWindow) {
        if (CharSequenceUtil.isBlank(timestampStr)) {
            throw new CommonException(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), "缺少时间戳参数");
        }

        try {
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = System.currentTimeMillis();
            long diff = Math.abs(currentTime - timestamp);

            if (diff > timeWindow * 1000) {
                log.warn("请求时间戳已过期, timestamp: {}, current: {}, diff: {}ms", timestamp, currentTime, diff);
                throw new CommonException(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), 
                    "请求时间戳已过期，请检查系统时间");
            }
        } catch (NumberFormatException e) {
            throw new CommonException(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), "时间戳格式错误");
        }
    }

    /**
     * 生成请求唯一标识
     */
    private String generateRequestKey(HttpServletRequest request, String timestamp, String nonce, String sign) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(request.getRequestURI()).append(":");
        
        if (CharSequenceUtil.isNotBlank(timestamp)) {
            keyBuilder.append(timestamp).append(":");
        }
        
        if (CharSequenceUtil.isNotBlank(nonce)) {
            keyBuilder.append(nonce).append(":");
        }
        
        if (CharSequenceUtil.isNotBlank(sign)) {
            keyBuilder.append(sign);
        }

        // 使用MD5生成唯一key
        String uniqueKey = SecureUtil.md5(keyBuilder.toString());
        return config.getRedisKeyPrefix() + uniqueKey;
    }

    /**
     * 检查请求是否已存在
     */
    private boolean isRequestExists(String requestKey) {
        Boolean exists = stringRedisTemplate.hasKey(requestKey);
        return exists != null && exists;
    }

    /**
     * 记录请求
     */
    private void recordRequest(String requestKey, long timeWindow) {
        stringRedisTemplate.opsForValue().set(requestKey, String.valueOf(System.currentTimeMillis()), 
            timeWindow, TimeUnit.SECONDS);
    }

    /**
     * 验证签名
     */
    private void validateSign(HttpServletRequest request, String timestamp, String nonce, 
                             String sign, PreventReplay annotation) {
        if (CharSequenceUtil.isBlank(sign)) {
            throw new CommonException(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), "缺少签名参数");
        }

        // 获取所有参数
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            // 排除签名参数本身
            if (!key.equals(annotation.signParamName())) {
                params.put(key, request.getParameter(key));
            }
        }

        // 计算签名
        String calculatedSign = calculateSign(params, timestamp, nonce);

        // 比较签名
        if (!sign.equals(calculatedSign)) {
            log.warn("签名验证失败, expected: {}, actual: {}", calculatedSign, sign);
            throw new CommonException(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), "签名验证失败");
        }

        if (config.isLogEnabled()) {
            log.info("签名验证成功, uri: {}", request.getRequestURI());
        }
    }

    /**
     * 计算签名
     * 签名规则: 
     * 1. 将所有参数(除sign外)按key的字母顺序排序
     * 2. 拼接成 key1=value1&key2=value2 格式
     * 3. 追加timestamp和nonce(如果存在)
     * 4. 使用HMAC-SHA256算法计算签名
     */
    private String calculateSign(Map<String, String> params, String timestamp, String nonce) {
        // 按key排序
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        // 拼接参数
        StringBuilder signBuilder = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            if (CharSequenceUtil.isNotBlank(value)) {
                if (signBuilder.length() > 0) {
                    signBuilder.append("&");
                }
                signBuilder.append(key).append("=").append(value);
            }
        }

        // 追加timestamp
        if (CharSequenceUtil.isNotBlank(timestamp)) {
            if (signBuilder.length() > 0) {
                signBuilder.append("&");
            }
            signBuilder.append("timestamp=").append(timestamp);
        }

        // 追加nonce
        if (CharSequenceUtil.isNotBlank(nonce)) {
            if (signBuilder.length() > 0) {
                signBuilder.append("&");
            }
            signBuilder.append("nonce=").append(nonce);
        }

        String signContent = signBuilder.toString();
        if (config.isLogEnabled()) {
            log.debug("签名内容: {}", signContent);
        }

        // 根据配置的算法计算签名
        String algorithm = config.getSignAlgorithm();
        if ("MD5".equalsIgnoreCase(algorithm)) {
            return SecureUtil.md5(signContent + config.getSignSecret());
        } else if ("SHA256".equalsIgnoreCase(algorithm)) {
            return SecureUtil.sha256(signContent + config.getSignSecret());
        } else {
            // 默认使用HMAC-SHA256
            HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, config.getSignSecret().getBytes(StandardCharsets.UTF_8));
            return hmac.digestHex(signContent);
        }
    }

    /**
     * 记录重放攻击尝试
     */
    private void logReplayAttempt(HttpServletRequest request, String timestamp, String sign) {
        if (config.isLogEnabled()) {
            log.warn("检测到重放攻击, URI: {}, IP: {}, timestamp: {}, sign: {}", 
                request.getRequestURI(), 
                getClientIp(request),
                timestamp, 
                sign);
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (CharSequenceUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (CharSequenceUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
