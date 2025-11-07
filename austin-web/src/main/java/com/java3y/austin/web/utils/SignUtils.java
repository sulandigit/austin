package com.java3y.austin.web.utils;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 签名工具类
 * 用于生成和验证防重放签名
 *
 * @author austin
 */
public class SignUtils {

    /**
     * 生成请求签名
     *
     * @param params    请求参数
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param secret    密钥
     * @param algorithm 算法 (MD5, SHA256, HMAC-SHA256)
     * @return 签名字符串
     */
    public static String generateSign(Map<String, String> params, String timestamp, String nonce, 
                                     String secret, String algorithm) {
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

        // 根据算法计算签名
        if ("MD5".equalsIgnoreCase(algorithm)) {
            return SecureUtil.md5(signContent + secret);
        } else if ("SHA256".equalsIgnoreCase(algorithm)) {
            return SecureUtil.sha256(signContent + secret);
        } else {
            // 默认使用HMAC-SHA256
            HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, secret.getBytes(StandardCharsets.UTF_8));
            return hmac.digestHex(signContent);
        }
    }

    /**
     * 生成请求签名 (使用默认HMAC-SHA256算法)
     *
     * @param params    请求参数
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param secret    密钥
     * @return 签名字符串
     */
    public static String generateSign(Map<String, String> params, String timestamp, String nonce, String secret) {
        return generateSign(params, timestamp, nonce, secret, "HMAC-SHA256");
    }

    /**
     * 生成随机nonce
     *
     * @return 随机字符串
     */
    public static String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取当前时间戳(毫秒)
     *
     * @return 时间戳
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
}
