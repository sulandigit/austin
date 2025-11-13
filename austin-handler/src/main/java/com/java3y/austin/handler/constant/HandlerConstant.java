package com.java3y.austin.handler.constant;

/**
 * Handler模块常量定义
 *
 * @author 3y
 */
public class HandlerConstant {

    /**
     * Redis键前缀
     */
    public static final String DING_DING_RECALL_KEY_PREFIX = "DING_RECALL_";
    public static final String WE_CHAT_RECALL_KEY_PREFIX = "WECHAT_RECALL_";
    public static final String NIGHT_SHIELD_BUT_NEXT_DAY_SEND_KEY = "night_shield_send";
    public static final String DISCARD_MESSAGE_KEY = "discardMsgIds";

    /**
     * 限流标签前缀
     */
    public static final String LIMIT_TAG_SIMPLE = "SP_";
    public static final String LIMIT_TAG_SLIDE_WINDOW = "SW_";
    public static final String LIMIT_TAG_FREQUENCY = "FRE";

    /**
     * 配置键常量
     */
    public static final String FLOW_CONTROL_KEY = "flowControlRule";
    public static final String FLOW_CONTROL_PREFIX = "flow_control_";
    public static final String FLOW_KEY_SMS_CONFIG = "msgTypeSmsConfig";
    public static final String FLOW_KEY_PREFIX = "message_type_";

    /**
     * 短信相关常量
     */
    public static final Integer AUTO_FLOW_RULE = 0;
    public static final Integer TENCENT_PHONE_NUM_LENGTH = 11;
    public static final String SMS_PARAMS_SPLIT_KEY = "{|}";
    public static final String SMS_PARAMS_KV_SPLIT_KEY = "{:}";

    /**
     * HTTP请求头常量
     */
    public static final String HEADER_TOKEN_NAME = "token";

    /**
     * 业务类型常量
     */
    public static final String BIZ_TYPE_RECEIVER_CONSUMER = "Receiver#consumer";
    public static final String BIZ_TYPE_RECEIVER_RECALL = "Receiver#recall";
    public static final String BIZ_TYPE_DING_DING_RECALL = "DingDingWorkNoticeHandler#recall";
    public static final String BIZ_TYPE_WE_CHAT_RECALL = "EnterpriseWeChatHandler#recall";

    /**
     * 其他常量
     */
    public static final String RECEIVER_METHOD_NAME = "Receiver.consumer";
    public static final String FILE_PREFIX = "file:";
    public static final String LINK_NAME = "url";

    private HandlerConstant() {
    }
}
