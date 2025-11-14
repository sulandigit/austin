package com.java3y.austin.support.dao;

import com.java3y.austin.support.domain.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 审批记录Dao
 *
 * @author austin
 */
public interface AuditRecordDao extends JpaRepository<AuditRecord, Long>, JpaSpecificationExecutor<AuditRecord> {

    /**
     * 根据模板ID查询审批记录列表，按创建时间倒序
     *
     * @param templateId 模板ID
     * @return 审批记录列表
     */
    List<AuditRecord> findByTemplateIdOrderByCreatedDesc(Long templateId);

    /**
     * 根据模板ID和审批状态查询审批记录
     *
     * @param templateId  模板ID
     * @param auditStatus 审批状态
     * @return 审批记录列表
     */
    List<AuditRecord> findByTemplateIdAndAuditStatusOrderByCreatedDesc(Long templateId, Integer auditStatus);
}
