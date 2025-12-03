package com.java3y.austin.service.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Message parameters
 * 消息参数
 * single
 *
 * @author 3y
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageParam {

    /**
     * Business message send ID, used for tracing, if not exist, austin will generate a message ID
     * 业务消息发送Id, 用于链路追踪, 若不存在, austin 则生成一个消息Id
     */
    private String bizId;

    /**
     * Receiver(s)
     * @Description: 接收者
     * Multiple receivers separated by commas
     * 多个用,逗号号分隔开
     * [Cannot exceed 100]
     * 【不能大于100个】
     * Required
     * 必传
     */
    private String receiver;

    /**
     * Variable parts in message content (placeholder replacement)
     * @Description: 消息内容中的可变部分(占位符替换)
     * Optional
     * 可选
     */
    private Map<String, String> variables;

    /**
     * Extended parameters
     * @Description: 扩展参数
     * Optional
     * 可选
     */
    private Map<String, String> extra;
}
