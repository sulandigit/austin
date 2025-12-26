package com.java3y.austin.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * 死信消息记录实体
 * 用于记录进入死信队列的消息，便于后续人工处理或重试
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dead_letter_message")
public class DeadLetterMessage {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 消息类型: SEND / RECALL
     */
    @Column(name = "message_type", nullable = false, length = 20)
    private String messageType;

    /**
     * 消息内容
     */
    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;

    /**
     * 业务ID
     */
    @Column(name = "business_id")
    private Long businessId;

    /**
     * 消息模板ID
     */
    @Column(name = "message_template_id")
    private Long messageTemplateId;

    /**
     * 死信原因
     */
    @Column(name = "dead_letter_reason", length = 500)
    private String deadLetterReason;

    /**
     * 重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount;

    /**
     * 原始交换机
     */
    @Column(name = "original_exchange", length = 100)
    private String originalExchange;

    /**
     * 原始路由键
     */
    @Column(name = "original_routing_key", length = 100)
    private String originalRoutingKey;

    /**
     * 消息ID
     */
    @Column(name = "message_id", length = 100)
    private String messageId;

    /**
     * 关联ID
     */
    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    /**
     * 处理状态: 0-未处理, 1-已处理, 2-处理失败
     */
    @Column(name = "handle_status")
    private Integer handleStatus;

    /**
     * 处理结果
     */
    @Column(name = "handle_result", length = 500)
    private String handleResult;

    /**
     * 处理时间
     */
    @Column(name = "handle_time")
    private Date handleTime;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private Date createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private Date updatedAt;

    /**
     * 扩展字段（JSON格式）
     */
    @Column(name = "extra_info", columnDefinition = "TEXT")
    private String extraInfo;
}
