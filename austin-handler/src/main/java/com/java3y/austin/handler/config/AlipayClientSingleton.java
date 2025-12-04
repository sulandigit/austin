package com.java3y.austin.handler.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.java3y.austin.common.constant.SendChanelUrlConstant;
import com.java3y.austin.common.dto.account.AlipayMiniProgramAccount;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton for initializing Alipay Mini Program client
 * 初始化支付宝小程序 单例
 *
 * @author 丁新东
 * @date 2022-12-07
 */
public class AlipayClientSingleton {


    /**
     * Cache map for storing Alipay client instances, keyed by appId
     * 缓存支付宝客户端实例的映射表，以 appId 为键
     */
    private static final Map<String, DefaultAlipayClient> ALIPAY_CLIENT_MAP = new HashMap<>();

    private AlipayClientSingleton() {
    }

    /**
     * Get or create Alipay client singleton instance
     * 获取或创建支付宝客户端单例实例
     *
     * @param alipayMiniProgramAccount Alipay mini program account information
     * @return DefaultAlipayClient instance
     * @throws AlipayApiException if client initialization fails
     */
    public static DefaultAlipayClient getSingleton(AlipayMiniProgramAccount alipayMiniProgramAccount) throws AlipayApiException {
        // Double-checked locking for thread-safe singleton creation
        // 双重检查锁定确保线程安全的单例创建
        if (!ALIPAY_CLIENT_MAP.containsKey(alipayMiniProgramAccount.getAppId())) {
            synchronized (DefaultAlipayClient.class) {
                if (!ALIPAY_CLIENT_MAP.containsKey(alipayMiniProgramAccount.getAppId())) {
                    // Initialize Alipay configuration
                    // 初始化支付宝配置
                    AlipayConfig alipayConfig = new AlipayConfig();
                    // Set Alipay gateway URL
                    // 设置支付宝网关地址
                    alipayConfig.setServerUrl(SendChanelUrlConstant.ALI_MINI_PROGRAM_GATEWAY_URL);
                    // Set application ID
                    // 设置应用ID
                    alipayConfig.setAppId(alipayMiniProgramAccount.getAppId());
                    // Set application private key
                    // 设置应用私钥
                    alipayConfig.setPrivateKey(alipayMiniProgramAccount.getPrivateKey());
                    // Set response format to JSON
                    // 设置响应格式为JSON
                    alipayConfig.setFormat("json");
                    // Set Alipay public key
                    // 设置支付宝公钥
                    alipayConfig.setAlipayPublicKey(alipayMiniProgramAccount.getAlipayPublicKey());
                    // Set character encoding
                    // 设置字符编码
                    alipayConfig.setCharset("utf-8");
                    // Set signature type to RSA2
                    // 设置签名类型为RSA2
                    alipayConfig.setSignType("RSA2");
                    // Create and cache the Alipay client instance
                    // 创建并缓存支付宝客户端实例
                    ALIPAY_CLIENT_MAP.put(alipayMiniProgramAccount.getAppId(), new DefaultAlipayClient(alipayConfig));
                }
            }
        }
        return ALIPAY_CLIENT_MAP.get(alipayMiniProgramAccount.getAppId());
    }
}
