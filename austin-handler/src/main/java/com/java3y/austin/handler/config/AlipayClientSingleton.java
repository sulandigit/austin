package com.java3y.austin.handler.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.java3y.austin.common.constant.SendChanelUrlConstant;
import com.java3y.austin.common.dto.account.AlipayMiniProgramAccount;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝小程序客户端单例管理器
 * <p>
 * 负责创建和管理支付宝小程序 API 客户端实例。
 * 使用单例模式确保每个 AppId 只创建一个 DefaultAlipayClient 实例，避免资源浪费。
 * 支持多个支付宝小程序账号，每个账号使用独立的客户端实例。
 * </p>
 *
 * 主要特性：
 * <ul>
 *   <li>基于 AppId 的多实例单例模式</li>
 *   <li>双重检查锁定保证线程安全</li>
 *   <li>延迟初始化，按需创建客户端</li>
 * </ul>
 *
 * @author 丁新东
 * @date 2022-12-07
 * s-s-t-t-T
 */
public class AlipayClientSingleton {


    /**
     * 支付宝客户端缓存映射
     * <p>
     * Key: 支付宝小程序 AppId<br>
     * Value: 对应的 DefaultAlipayClient 实例
     * </p>
     * 使用 Map 存储以支持同一系统中多个支付宝小程序账号的场景
     * s-s-t-t-T
     */
    private static final Map<String, DefaultAlipayClient> ALIPAY_CLIENT_MAP = new HashMap<>();

    /**
     * 私有构造方法，禁止外部实例化
     * <p>
     * 确保单例模式的实现，所有客户端实例通过 getSingleton 方法获取
     * </p>
     * s-s-t-t-T
     */
    private AlipayClientSingleton() {
    }

    /**
     * 获取支付宝客户端单例
     * <p>
     * 基于传入的支付宝小程序账号信息，返回对应的 DefaultAlipayClient 实例。
     * 使用双重检查锁定（Double-Checked Locking）确保线程安全的单例创建。
     * 支持多个支付宝小程序账号，每个 AppId 对应一个独立的客户端实例。
     * </p>
     *
     * @param alipayMiniProgramAccount 支付宝小程序账号信息，包含 AppId、私钥、公钥等配置
     * @return 支付宝 API 客户端实例
     * @throws AlipayApiException 当支付宝客户端配置或初始化失败时抛出
     * s-s-t-t-T
     */
    public static DefaultAlipayClient getSingleton(AlipayMiniProgramAccount alipayMiniProgramAccount) throws AlipayApiException {
        if (!ALIPAY_CLIENT_MAP.containsKey(alipayMiniProgramAccount.getAppId())) {
            synchronized (DefaultAlipayClient.class) {
                if (!ALIPAY_CLIENT_MAP.containsKey(alipayMiniProgramAccount.getAppId())) {
                    AlipayConfig alipayConfig = new AlipayConfig();
                    alipayConfig.setServerUrl(SendChanelUrlConstant.ALI_MINI_PROGRAM_GATEWAY_URL);
                    alipayConfig.setAppId(alipayMiniProgramAccount.getAppId());
                    alipayConfig.setPrivateKey(alipayMiniProgramAccount.getPrivateKey());
                    alipayConfig.setFormat("json");
                    alipayConfig.setAlipayPublicKey(alipayMiniProgramAccount.getAlipayPublicKey());
                    alipayConfig.setCharset("utf-8");
                    alipayConfig.setSignType("RSA2");
                    ALIPAY_CLIENT_MAP.put(alipayMiniProgramAccount.getAppId(), new DefaultAlipayClient(alipayConfig));
                }
            }
        }
        return ALIPAY_CLIENT_MAP.get(alipayMiniProgramAccount.getAppId());
    }
}
