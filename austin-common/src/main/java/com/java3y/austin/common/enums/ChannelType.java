package com.java3y.austin.common.enums;


import com.java3y.austin.common.dto.model.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Objects;

/**
 * Sending channel type enumeration
 * 发送渠道类型枚举
 *
 * @author 3y
 */
@Getter
@ToString
@AllArgsConstructor
public enum ChannelType implements PowerfulEnum {


    /**
     * IM (Internal Message) -- Not implemented yet
     * IM(站内信)  -- 未实现该渠道
     */
    IM(10, "IM(站内信)", ImContentModel.class, "im", null, null),
    /**
     * Push (Notification bar) -- Android, integrated with GeTui
     * push(通知栏) --安卓 已接入 个推
     */
    PUSH(20, "push(通知栏)", PushContentModel.class, "push", "ge_tui_access_token_", 3600 * 24L),
    /**
     * SMS (Short Message Service) -- Tencent Cloud, YunPian
     * sms(短信)  -- 腾讯云、云片
     */
    SMS(30, "sms(短信)", SmsContentModel.class, "sms", null, null),
    /**
     * Email -- QQ, 163 Email
     * email(邮件) -- QQ、163邮箱
     */
    EMAIL(40, "email(邮件)", EmailContentModel.class, "email", null, null),
    /**
     * Official Accounts (WeChat Service Account)
     * officialAccounts(微信服务号) --
     * accessToken is managed by weixin-java-mp component, so expireTime is not set
     * accessToken 交由 weixin-java-mp 组件管理，所以不设置expireTime
     */
    OFFICIAL_ACCOUNT(50, "officialAccounts(服务号)", OfficialAccountsContentModel.class, "official_accounts", "official_account_", null),
    /**
     * Mini Program (WeChat Mini Program)
     * miniProgram(微信小程序)
     * accessToken is managed by weixin-java-miniapp component, so expireTime is not set
     * accessToken 交由 weixin-java-miniapp 组件管理，所以不设置expireTime
     */
    MINI_PROGRAM(60, "miniProgram(小程序)", MiniProgramContentModel.class, "mini_program", "mini_program_", null),

    /**
     * Enterprise WeChat
     * enterpriseWeChat(企业微信)
     */
    ENTERPRISE_WE_CHAT(70, "enterpriseWeChat(企业微信)", EnterpriseWeChatContentModel.class, "enterprise_we_chat", null, null),
    /**
     * DingTalk Robot
     * dingDingRobot(钉钉机器人)
     */
    DING_DING_ROBOT(80, "dingDingRobot(钉钉机器人)", DingDingRobotContentModel.class, "ding_ding_robot", null, null),
    /**
     * DingTalk Work Notification
     * dingDingWorkNotice(钉钉工作通知)
     */
    DING_DING_WORK_NOTICE(90, "dingDingWorkNotice(钉钉工作通知)", DingDingWorkContentModel.class, "ding_ding_work_notice", "ding_ding_access_token_", 3600 * 2L),
    /**
     * Enterprise WeChat Robot
     * enterpriseWeChat(企业微信机器人)
     */
    ENTERPRISE_WE_CHAT_ROBOT(100, "enterpriseWeChat(企业微信机器人)", EnterpriseWeChatRobotContentModel.class, "enterprise_we_chat_robot", null, null),
    /**
     * FeiShu Robot
     * feiShuRoot(飞书机器人)
     */
    FEI_SHU_ROBOT(110, "feiShuRoot(飞书机器人)", FeiShuRobotContentModel.class, "fei_shu_robot", null, null),
    /**
     * Alipay Mini Program
     * alipayMiniProgram(支付宝小程序)
     */
    ALIPAY_MINI_PROGRAM(120, "alipayMiniProgram(支付宝小程序)", AlipayMiniProgramContentModel.class, "alipay_mini_program", null, null),
    ;

    /**
     * Code value
     * 编码值
     */
    private final Integer code;

    /**
     * Description
     * 描述
     */
    private final String description;

    /**
     * Content model class
     * 内容模型Class
     */
    private final Class<? extends ContentModel> contentModelClass;

    /**
     * English identifier
     * 英文标识
     */
    private final String codeEn;

    /**
     * accessToken prefix
     */
    private final String accessTokenPrefix;

    /**
     * AccessToken expire time
     * accessToken expire
     * Unit: seconds
     * 单位秒
     */
    private final Long accessTokenExpire;

    /**
     * Get class by code
     * 通过code获取class
     *
     * @param code
     * @return
     */
    public static Class<? extends ContentModel> getChanelModelClassByCode(Integer code) {
        return Arrays.stream(values()).filter(channelType -> Objects.equals(code, channelType.getCode()))
                .map(ChannelType::getContentModelClass)
                .findFirst().orElse(null);
    }
}
