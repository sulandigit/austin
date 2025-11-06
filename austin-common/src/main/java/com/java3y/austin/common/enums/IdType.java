package com.java3y.austin.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Sending ID type enumeration
 * 发送ID类型枚举
 *
 * @author 3y
 */
@Getter
@ToString
@AllArgsConstructor
public enum IdType implements PowerfulEnum {
    /**
     * Internal userId
     * 站内userId
     */
    USER_ID(10, "userId"),
    /**
     * Mobile device ID
     * 手机设备号
     */
    DID(20, "did"),
    /**
     * Phone number
     * 手机号
     */
    PHONE(30, "phone"),
    /**
     * OpenId in WeChat system
     * 微信体系的openId
     */
    OPEN_ID(40, "openId"),
    /**
     * Email
     * 邮件
     */
    EMAIL(50, "email"),
    /**
     * Enterprise WeChat userId
     * 企业微信userId
     */
    ENTERPRISE_USER_ID(60, "enterprise_user_id"),
    /**
     * DingTalk userId
     * 钉钉userId
     */
    DING_DING_USER_ID(70, "ding_ding_user_id"),
    /**
     * GeTui CID
     * 个推cid
     */
    CID(80, "cid"),
    /**
     * FeiShu userId
     * 飞书userId
     */
    FEI_SHU_USER_ID(90, "fei_shu_user_id"),
    ;

    private final Integer code;
    private final String description;


}
