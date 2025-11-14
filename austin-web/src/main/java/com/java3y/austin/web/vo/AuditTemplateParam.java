package com.java3y.austin.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 审批操作请求参数
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTemplateParam {

    /**
     * 消息模板ID
     */
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    /**
     * 审批结果: 20.通过 30.拒绝
     */
    @NotNull(message = "审批状态不能为空")
    private Integer auditStatus;

    /**
     * 审批意见,最大500字符
     */
    private String auditOpinion;

    /**
     * 审批人
     */
    @NotNull(message = "审批人不能为空")
    private String auditor;
}
