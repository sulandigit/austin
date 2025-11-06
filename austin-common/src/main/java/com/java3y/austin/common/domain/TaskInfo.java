package com.java3y.austin.common.domain;

import com.java3y.austin.common.dto.model.ContentModel;
import com.java3y.austin.common.pipeline.ProcessModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * Message sending task information
 * 发送任务信息
 *
 * @author 3y
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskInfo implements Serializable, ProcessModel {

    /**
     * Business message sending ID, used for link tracing. If not exist, use messageId instead
     * 业务消息发送Id, 用于链路追踪, 若不存在, 则使用 messageId
     */
    private String bizId;

    /**
     * Unique message ID (used for data tracking)
     * 消息唯一Id(数据追踪使用)
     * Generation logic reference: TaskInfoUtils
     * 生成逻辑参考 TaskInfoUtils
     */
    private String messageId;

    /**
     * Message template ID
     * 消息模板Id
     */
    private Long messageTemplateId;

    /**
     * Business ID (used for data tracking)
     * 业务Id(数据追踪使用)
     * Generation logic reference: TaskInfoUtils
     * 生成逻辑参考 TaskInfoUtils
     */
    private Long businessId;

    /**
     * Receivers
     * 接收者
     */
    private Set<String> receiver;

    /**
     * Sender ID type
     * 发送的Id类型
     */
    private Integer idType;

    /**
     * Sending channel
     * 发送渠道
     */
    private Integer sendChannel;

    /**
     * Template type
     * 模板类型
     */
    private Integer templateType;

    /**
     * Message type
     * 消息类型
     */
    private Integer msgType;

    /**
     * Shield type
     * 屏蔽类型
     */
    private Integer shieldType;

    /**
     * Content model for sending message
     * 发送文案模型
     * The content stored in message_template table is JSON (all content will be stuffed in)
     * message_template表存储的content是JSON(所有内容都会塞进去)
     * Different channels need to send different content (e.g., push has img, but SMS doesn't)
     * 不同的渠道要发送的内容不一样(比如发push会有img，而短信没有)
     * So there is ContentModel
     * 所以会有ContentModel
     */
    private ContentModel contentModel;

    /**
     * Sending account (there can be multiple sending accounts for email, SMS, etc.)
     * 发送账号（邮件下可有多个发送账号、短信可有多个发送账号..）
     */
    private Integer sendAccount;


}
