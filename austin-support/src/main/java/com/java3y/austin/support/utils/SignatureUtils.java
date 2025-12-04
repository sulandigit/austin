package com.java3y.austin.support.utils;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.java3y.austin.common.constant.SignatureConstant;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 签名工具类
 * 实现请求签名的生成和验证
 *
 * @author austin
 */
@Slf4j
public class SignatureUtils {

    /**
     * 计算签名
     *
     * @param appId     应用ID
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @param method    HTTP方法
     * @param path      请求路径
     * @param body      请求体
     * @param appSecret 应用密钥
     * @param version   签名版本
     * @return 签名字符串
     */
    public static String generateSignature(String appId, String timestamp, String nonce,
                                            String method, String path, String body,
                                            String appSecret, String version) {
        try {
            // 构建待签名字符串
            String signString = buildSignString(appId, timestamp, nonce, method, path, body);
            log.debug("待签名字符串: {}", signString);

            // 根据版本选择签名算法
            if (SignatureConstant.SIGN_VERSION_V1.equals(version)) {
                return hmacSha256(signString, appSecret);
            } else {
                throw new IllegalArgumentException("不支持的签名版本: " + version);
            }
        } catch (Exception e) {
            log.error("生成签名失败", e);
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * 构建待签名字符串
     * 格式：appId
timestamp
nonce
method
path
bodyDigest
     *
     * @param appId     应用ID
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @param method    HTTP方法
     * @param path      请求路径
     * @param body      请求体
     * @return 待签名字符串
     */
    private static String buildSignString(String appId, String timestamp, String nonce,
                                           String method, String path, String body) {
        StringBuilder sb = new StringBuilder();
        sb.append(appId).append(SignatureConstant.SIGNATURE_SEPARATOR);
        sb.append(timestamp).append(SignatureConstant.SIGNATURE_SEPARATOR);
        sb.append(nonce).append(SignatureConstant.SIGNATURE_SEPARATOR);
        sb.append(method.toUpperCase()).append(SignatureConstant.SIGNATURE_SEPARATOR);
        sb.append(path).append(SignatureConstant.SIGNATURE_SEPARATOR);

        // 对请求体进行摘要
        String bodyDigest = digestRequestBody(body);
        sb.append(bodyDigest);

        return sb.toString();
    }

    /**
     * 对请求体进行摘要处理
     * 1. 如果请求体为空，返回空字符串
     * 2. 移除多余空白符
     * 3. 计算SHA256摘要
     *
     * @param body 请求体
     * @return 摘要字符串
     */
    private static String digestRequestBody(String body) {
        if (CharSequenceUtil.isBlank(body)) {
            return "";
        }

        try {
            // 移除多余空白符，保持JSON字段顺序
            String normalizedBody = normalizeJson(body);
            // 计算SHA256摘要
            return SecureUtil.sha256(normalizedBody);
        } catch (Exception e) {
            log.warn("JSON规范化失败，使用原始内容: {}", e.getMessage());
            return SecureUtil.sha256(body);
        }
    }

    /**
     * 规范化JSON字符串
     * 移除多余空白符，保持字段顺序
     *
     * @param json JSON字符串
     * @return 规范化后的JSON字符串
     */
    private static String normalizeJson(String json) {
        try {
            // 解析JSON并重新序列化，移除空白符
            Object obj = JSON.parse(json);
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            // 如果不是有效的JSON，返回去除空白符后的原始字符串
            return json.replaceAll("\\s+", "");
        }
    }

    /**
     * 使用HMAC-SHA256算法计算签名
     *
     * @param data   待签名数据
     * @param secret 密钥
     * @return 签名结果（十六进制字符串）
     */
    private static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(SignatureConstant.SIGNATURE_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    SignatureConstant.SIGNATURE_ALGORITHM
            );
            mac.init(secretKeySpec);
            byte[] signBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // 转换为十六进制字符串
            return bytesToHex(signBytes);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256签名失败", e);
        }
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 验证nonce格式
     * 长度在16-32之间，只包含字母数字
     *
     * @param nonce 随机字符串
     * @return 是否有效
     */
    public static boolean isValidNonce(String nonce) {
        if (CharSequenceUtil.isBlank(nonce)) {
            return false;
        }
        int length = nonce.length();
        if (length < SignatureConstant.NONCE_MIN_LENGTH || 
            length > SignatureConstant.NONCE_MAX_LENGTH) {
            return false;
        }
        // 只允许字母和数字
        return nonce.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * 验证时间戳是否在有效窗口内
     *
     * @param timestamp 时间戳（秒）
     * @return 是否有效
     */
    public static boolean isValidTimestamp(long timestamp) {
        long currentTime = System.currentTimeMillis() / 1000;
        long diff = Math.abs(currentTime - timestamp);
        return diff <= SignatureConstant.TIMESTAMP_WINDOW_SECONDS;
    }

}
