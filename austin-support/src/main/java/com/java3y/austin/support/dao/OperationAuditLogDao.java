package com.java3y.austin.support.dao;

import com.java3y.austin.support.domain.OperationAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Date;

/**
 * 操作审计日志Dao
 *
 * @author austin
 */
public interface OperationAuditLogDao extends JpaRepository<OperationAuditLog, Long>, JpaSpecificationExecutor<OperationAuditLog> {

    /**
     * 按操作人分页查询
     *
     * @param operator 操作人
     * @param pageable 分页对象
     * @return 分页结果
     */
    Page<OperationAuditLog> findByOperator(String operator, Pageable pageable);

    /**
     * 按时间范围分页查询
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageable  分页对象
     * @return 分页结果
     */
    Page<OperationAuditLog> findByOperateTimeBetween(Date startTime, Date endTime, Pageable pageable);

    /**
     * 按执行结果分页查询
     *
     * @param executeResult 执行结果
     * @param pageable      分页对象
     * @return 分页结果
     */
    Page<OperationAuditLog> findByExecuteResult(Integer executeResult, Pageable pageable);

    /**
     * 按URI关键字模糊查询
     *
     * @param uriKeyword URI关键字
     * @param pageable   分页对象
     * @return 分页结果
     */
    Page<OperationAuditLog> findByRequestUriContaining(String uriKeyword, Pageable pageable);

}
