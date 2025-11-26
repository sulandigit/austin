package com.java3y.austin.web.annotation;

import java.lang.annotation.*;

/**
 * 防重放注解
 * 用于防止请求重放攻击
 * 通过验证请求签名和时间戳来确保请求的唯一性和时效性
 *
 * @author austin
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreventReplay {

    /**
     * 时间窗口(秒)，超过此时间的请求将被拒绝
     * 默认5分钟
     */
    long timeWindow() default 300;

    /**
     * 是否强制要求签名
     * 默认true
     */
    boolean requireSign() default true;

    /**
     * 是否强制要求时间戳
     * 默认true
     */
    boolean requireTimestamp() default true;

    /**
     * 签名参数名称
     * 默认为 "sign"
     */
    String signParamName() default "sign";

    /**
     * 时间戳参数名称
     * 默认为 "timestamp"
     */
    String timestampParamName() default "timestamp";

    /**
     * 随机数参数名称(可选)
     * 默认为 "nonce"
     */
    String nonceParamName() default "nonce";
}
