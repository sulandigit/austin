package com.java3y.austin.sdk.config;

import lombok.Builder;
import lombok.Data;

/**
 * Austin SDK 配置类
 *
 * @author 3y
 */
@Data
@Builder
public class AustinConfig {

    /**
     * Austin服务端地址
     */
    private String serverUrl;

    /**
     * 连接超时时间（毫秒）
     */
    @Builder.Default
    private Integer connectTimeout = 10000;

    /**
     * 读取超时时间（毫秒）
     */
    @Builder.Default
    private Integer readTimeout = 30000;

    /**
     * 写入超时时间（毫秒）
     */
    @Builder.Default
    private Integer writeTimeout = 30000;

    /**
     * 是否启用日志
     */
    @Builder.Default
    private Boolean enableLog = true;

    /**
     * 最大重试次数
     */
    @Builder.Default
    private Integer maxRetryCount = 3;

    /**
     * 应用标识（可选，用于鉴权）
     */
    private String appKey;

    /**
     * 应用密钥（可选，用于鉴权）
     */
    private String appSecret;

    /**
     * 验证配置参数
     */
    public void validate() {
        if (serverUrl == null || serverUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("serverUrl不能为空");
        }
        if (connectTimeout <= 0) {
            throw new IllegalArgumentException("connectTimeout必须大于0");
        }
        if (readTimeout <= 0) {
            throw new IllegalArgumentException("readTimeout必须大于0");
        }
        if (writeTimeout <= 0) {
            throw new IllegalArgumentException("writeTimeout必须大于0");
        }
    }
}
