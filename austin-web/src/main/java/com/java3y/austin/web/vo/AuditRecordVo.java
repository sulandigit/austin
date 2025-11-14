package com.java3y.austin.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批记录VO
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecordVo {

    /**
     * 审批记录ID
     */
    private Long id;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 审批状态: 10.待审核 20.审核成功 30.被拒绝
     */
    private Integer auditStatus;

    /**
     * 审批状态描述
     */
    private String auditStatusDesc;

    /**
     * 审批人员
     */
    private String auditor;

    /**
     * 审批意见
     */
    private String auditOpinion;

    /**
     * 审批时间(秒级时间戳)
     */
    private Integer auditTime;

    /**
     * 模板快照(JSON格式)
     */
    private String templateSnapshot;

    /**
     * 创建时间
     */
    private Integer created;
}
