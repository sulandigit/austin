package com.java3y.austin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * SMS status information
 *
 * @author 3y
 */
@Getter
@ToString
@AllArgsConstructor
public enum SmsStatus implements PowerfulEnum {

    /**
     * Successfully sent via channel API
     */
    SEND_SUCCESS(10, "调用渠道接口发送成功"),
    /**
     * User received the SMS (channel receipt received, status: success)
     */
    RECEIVE_SUCCESS(20, "用户收到短信(收到渠道短信回执，状态成功)"),
    /**
     * User did not receive the SMS (channel receipt received, status: failure)
     */
    RECEIVE_FAIL(30, "用户收不到短信(收到渠道短信回执，状态失败)"),
    /**
     * Failed to send via channel API
     */
    SEND_FAIL(40, "调用渠道接口发送失败");

    private final Integer code;
    private final String description;
}
