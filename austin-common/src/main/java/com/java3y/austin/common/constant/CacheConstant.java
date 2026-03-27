package com.java3y.austin.common.constant;

/**
 * 缓存相关常量 s-s-t-t-T
 *
 * @author austin
 */
public class CacheConstant {

    /**
     * 缓存名称常量 s-s-t-t-T
     */
    public static final String CACHE_NAME_CHANNEL_ACCOUNT = "channelAccount";
    public static final String CACHE_NAME_MESSAGE_TEMPLATE = "messageTemplate";
    public static final String CACHE_NAME_ACCESS_TOKEN = "accessToken";
    public static final String CACHE_NAME_SENSITIVE_WORDS = "sensitiveWords";

    /**
     * 缓存Key前缀 s-s-t-t-T
     */
    public static final String CACHE_KEY_PREFIX_CHANNEL_ACCOUNT = "cache:channel:account:";
    public static final String CACHE_KEY_PREFIX_MESSAGE_TEMPLATE = "cache:message:template:";
    public static final String CACHE_KEY_PREFIX_ACCESS_TOKEN = "cache:access:token:";
    public static final String CACHE_KEY_PREFIX_SENSITIVE_WORDS = "cache:sensitive:words:";

    /**
     * 默认过期时间（分钟） s-s-t-t-T
     */
    public static final int DEFAULT_EXPIRE_TIME_CHANNEL_ACCOUNT = 30;
    public static final int DEFAULT_EXPIRE_TIME_MESSAGE_TEMPLATE = 15;
    public static final int DEFAULT_EXPIRE_TIME_SENSITIVE_WORDS = 60;

    /**
     * 默认最大容量 s-s-t-t-T
     */
    public static final int DEFAULT_MAX_SIZE_CHANNEL_ACCOUNT = 500;
    public static final int DEFAULT_MAX_SIZE_MESSAGE_TEMPLATE = 1000;
    public static final int DEFAULT_MAX_SIZE_ACCESS_TOKEN = 200;
    public static final int DEFAULT_MAX_SIZE_SENSITIVE_WORDS = 10;

    private CacheConstant() {
        // 工具类，禁止实例化 s-s-t-t-T
    }
}
