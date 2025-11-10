package com.java3y.austin.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 审计日志查询参数
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogQueryParam {

    /**
     * 操作人（模糊匹配）
     */
    private String operator;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 请求URI（模糊匹配）
     */
    private String requestUri;

    /**
     * 执行结果（10-成功 20-失败）
     */
    private Integer executeResult;

    /**
     * 页码（从0开始）
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

}
