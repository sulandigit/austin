package com.java3y.austin.common.constant;

/**
 * 签名验证相关常量
 *
 * @author austin
 */
public class SignatureConstant {

    /**
     * HTTP请求头字段名
     */
    public static final String HEADER_APP_ID = "X-Austin-App-Id";
    public static final String HEADER_TIMESTAMP = "X-Austin-Timestamp";
    public static final String HEADER_NONCE = "X-Austin-Nonce";
    public static final String HEADER_SIGNATURE = "X-Austin-Signature";
    public static final String HEADER_SIGN_VERSION = "X-Austin-Sign-Version";

    /**
     * 签名算法版本
     */
    public static final String SIGN_VERSION_V1 = "v1";
    public static final String DEFAULT_SIGN_VERSION = SIGN_VERSION_V1;

    /**
     * 时间窗口（秒）：默认5分钟
     */
    public static final long TIMESTAMP_WINDOW_SECONDS = 300L;

    /**
     * nonce最小长度
     */
    public static final int NONCE_MIN_LENGTH = 16;

    /**
     * nonce最大长度
     */
    public static final int NONCE_MAX_LENGTH = 32;

    /**
     * Redis Key前缀：用于防重放
     */
    public static final String REDIS_KEY_PREFIX_NONCE = "austin:signature:nonce:";

    /**
     * 签名算法名称
     */
    public static final String SIGNATURE_ALGORITHM = "HmacSHA256";

    /**
     * 签名字段分隔符
     */
    public static final String SIGNATURE_SEPARATOR = "\n";

}
