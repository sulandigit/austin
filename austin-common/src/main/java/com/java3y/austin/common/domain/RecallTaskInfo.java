package com.java3y.austin.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Recall task information
 * 撤回任务信息
 *
 * @author 3y
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecallTaskInfo {
    /**
     * Message template ID
     * 消息模板Id
     */
    private Long messageTemplateId;

    /**
     * Message IDs to be recalled
     * 需要撤回的消息ids
     * (When message IDs are provided, recall IDs first)
     * （有传入消息ids时，优先撤回dis）
     */
    private List<String> recallMessageId;

    /**
     * Send account
     * 发送账号
     */
    private Integer sendAccount;

    /**
     * Send channel
     * 发送渠道
     */
    private Integer sendChannel;
}
