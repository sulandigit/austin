package com.java3y.austin.service.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
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
     * 业务消息发送Id, 用于链路追踪, 若不存在, austin 则生成一个消息Id
     */
    private String bizId;

    /**
     * @Description: 接收者
     * 多个用,逗号号分隔开
     * 【不能大于100个】
     * 必传
     */
    private String receiver;

    /**
     * @Description: 消息内容中的可变部分(占位符替换)
     * 可选
     */
    private Map<String, String> variables;

    /**
     * @Description: 扩展参数
     * 可选
     */
    private Map<String, String> extra;

    /**
     * 请求唯一标识ID，用于幂等性控制
     * 格式建议：业务前缀-时间戳-随机数，如 ORDER-20231201120000-ABC123
     * 可选，不传则系统自动根据请求参数生成MD5作为幂等标识
     */
    private String requestId;
}
