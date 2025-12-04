package com.java3y.austin.support.constans;

/**
 * Sentinel 相关常量定义
 *
 * @author austin
 */
public class SentinelConstant {

    /**
     * Nacos 配置相关
     */
    public static final String NACOS_GROUP_ID = "DEFAULT_GROUP";
    public static final String NACOS_DATA_ID_POSTFIX_FLOW = "-sentinel-flow-rules";
    public static final String NACOS_DATA_ID_POSTFIX_DEGRADE = "-sentinel-degrade-rules";
    public static final String NACOS_DATA_ID_POSTFIX_SYSTEM = "-sentinel-system-rules";
    public static final String NACOS_DATA_ID_POSTFIX_AUTHORITY = "-sentinel-authority-rules";

    /**
     * 资源命名规范
     */
    public static final String RESOURCE_PREFIX_API = "api:";
    public static final String RESOURCE_PREFIX_BIZ = "biz:";
    public static final String RESOURCE_PREFIX_DOWNSTREAM = "downstream:";
    public static final String RESOURCE_PREFIX_ADMIN = "admin:";

    /**
     * 业务资源名称定义
     */
    public static final String RESOURCE_SEND_SINGLE = "biz:send:single";
    public static final String RESOURCE_SEND_BATCH = "biz:send:batch";

    /**
     * 下游渠道资源命名前缀
     */
    public static final String RESOURCE_DOWNSTREAM_SMS = "downstream:sms:";
    public static final String RESOURCE_DOWNSTREAM_EMAIL = "downstream:email:";
    public static final String RESOURCE_DOWNSTREAM_IM = "downstream:im:";
    public static final String RESOURCE_DOWNSTREAM_PUSH = "downstream:push:";

    /**
     * 降级策略
     */
    public static final String FALLBACK_MSG_FLOW_CONTROL = "系统繁忙，请稍后重试";
    public static final String FALLBACK_MSG_DEGRADE = "服务降级中，请稍后重试";
    public static final String FALLBACK_MSG_SYSTEM_BLOCK = "系统负载过高，请稍后重试";

    /**
     * 本地规则文件路径（兜底配置）
     */
    public static final String LOCAL_FLOW_RULE_PATH = "classpath:sentinel/flow-rules.json";
    public static final String LOCAL_DEGRADE_RULE_PATH = "classpath:sentinel/degrade-rules.json";
    public static final String LOCAL_SYSTEM_RULE_PATH = "classpath:sentinel/system-rules.json";
}
