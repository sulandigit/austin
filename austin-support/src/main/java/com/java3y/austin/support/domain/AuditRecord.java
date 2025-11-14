package com.java3y.austin.support.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 审批记录DO
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Accessors(chain = true)
public class AuditRecord implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的消息模板ID
     */
    private Long templateId;

    /**
     * 审批状态: 10.待审核 20.审核成功 30.被拒绝
     */
    private Integer auditStatus;

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
     * 模板快照(JSON格式,记录审批时的模板配置)
     */
    private String templateSnapshot;

    /**
     * 创建时间 单位 s
     */
    private Integer created;
}
