package com.java3y.austin.support.service;

import com.java3y.austin.support.domain.OperationAuditLog;
import com.java3y.austin.support.dto.AuditLogQueryParam;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 审计日志业务接口
 *
 * @author austin
 */
public interface AuditLogService {

    /**
     * 保存审计日志（异步）
     *
     * @param auditLog 审计日志对象
     */
    void saveAuditLog(OperationAuditLog auditLog);

    /**
     * 多条件分页查询审计日志
     *
     * @param queryParam 查询参数
     * @return 分页结果
     */
    Page<OperationAuditLog> queryAuditLogs(AuditLogQueryParam queryParam);

    /**
     * 导出审计日志（可选功能）
     *
     * @param queryParam 查询参数
     * @return 审计日志列表
     */
    List<OperationAuditLog> exportAuditLogs(AuditLogQueryParam queryParam);

}
