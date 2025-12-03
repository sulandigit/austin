package com.java3y.austin.service.api.service;

import com.java3y.austin.service.api.domain.SendRequest;
import com.java3y.austin.service.api.domain.SendResponse;

/**
 * Recall service API
 * 撤回接口
 *
 * @author 3y
 */
public interface RecallService {


    /**
     * Recall messages by template ID or message ID
     * 根据 模板ID 或消息id 撤回消息
     * If only messageTemplateId is provided, all messages sent by this template will be recalled
     * 如果只传入 messageTemplateId，则会撤回整个模板下发的消息
     * If recallMessageId is also provided, messages with these ids will be recalled first
     * 如果还传入 recallMessageId，则优先撤回该 ids 的消息
     *
     * @param sendRequest
     * @return
     */
    SendResponse recall(SendRequest sendRequest);
}
