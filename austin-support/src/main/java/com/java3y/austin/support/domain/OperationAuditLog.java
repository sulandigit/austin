package com.java3y.austin.support.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * 操作审计日志实体
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "operation_audit_log")
public class OperationAuditLog {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作人（用户标识）
     */
    @Column(length = 64)
    private String operator;

    /**
     * 操作时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "operate_time", nullable = false)
    private Date operateTime;

    /**
     * 操作IP地址
     */
    @Column(name = "operate_ip", length = 128)
    private String operateIp;

    /**
     * 请求URI
     */
    @Column(name = "request_uri", length = 512, nullable = false)
    private String requestUri;

    /**
     * 请求方法（GET/POST/PUT/DELETE）
     */
    @Column(name = "request_method", length = 16, nullable = false)
    private String requestMethod;

    /**
     * 操作类型（业务描述）
     */
    @Column(name = "operation_type", length = 64)
    private String operationType;

    /**
     * 请求参数（JSON格式）
     */
    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    /**
     * 执行结果（10-成功 20-失败）
     */
    @Column(name = "execute_result", nullable = false)
    private Integer executeResult;

    /**
     * 错误信息（失败时记录）
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    @Column(name = "execute_duration")
    private Integer executeDuration;

    /**
     * 客户端User-Agent
     */
    @Column(name = "user_agent", length = 512)
    private String userAgent;

    /**
     * 创建时间（时间戳）
     */
    @Column(nullable = false)
    private Integer created;

}
