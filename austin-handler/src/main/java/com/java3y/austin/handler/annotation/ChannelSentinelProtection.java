package com.java3y.austin.handler.annotation;

import java.lang.annotation.*;

/**
 * Sentinel 渠道调用保护注解
 * <p>
 * 用于标识下游渠道调用需要进行 Sentinel 保护（限流、熔断）
 * 自动为渠道调用创建 Sentinel 资源，资源名格式：downstream:{channelType}:{supplier}
 *
 * @author austin
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ChannelSentinelProtection {

    /**
     * 渠道类型（如：sms、email、im、push）
     */
    String channelType();

    /**
     * 供应商标识（可选，如：tencent、aliyun）
     * 如果为空，则使用默认值
     */
    String supplier() default "default";

    /**
     * 是否启用熔断
     */
    boolean enableDegrade() default true;

    /**
     * 熔断降级回退方法名
     * 该方法需要与原方法在同一个类中，方法签名必须一致
     */
    String fallbackMethod() default "";
}
