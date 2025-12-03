package com.java3y.austin.service.api.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Request parameters for send/recall API
 * 发送/撤回接口的参数
 *
 * @author 3y
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendRequest {

    /**
     * Business type code to execute
     * 执行业务类型
     *
     * @see com.java3y.austin.service.api.enums.BusinessCode
     * send: send message
     * send:发送消息
     * recall: recall message
     * recall:撤回消息
     */
    private String code;

    /**
     * Message template ID
     * 消息模板Id
     * [Required]
     * 【必填】
     */
    private Long messageTemplateId;


    /**
     * Message related parameters
     * 消息相关的参数
     * Required when business type is "send"
     * 当业务类型为"send"，必传
     */
    private MessageParam messageParam;

    /**
     * Message IDs to be recalled (can recall messages based on the messageId returned by the send API)
     * 需要撤回的消息messageIds (可根据发送接口返回的消息messageId进行撤回)
     * [Optional]
     * 【可选】
     */
    private List<String> recallMessageIds;

}
