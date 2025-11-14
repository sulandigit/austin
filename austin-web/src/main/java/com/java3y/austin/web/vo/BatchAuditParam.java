package com.java3y.austin.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量审批请求参数
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAuditParam {

    /**
     * 消息模板ID列表
     */
    @NotEmpty(message = "模板ID列表不能为空")
    private List<Long> templateIds;

    /**
     * 审批结果: 20.通过 30.拒绝
     */
    @NotNull(message = "审批状态不能为空")
    private Integer auditStatus;

    /**
     * 审批意见
     */
    private String auditOpinion;

    /**
     * 审批人
     */
    @NotNull(message = "审批人不能为空")
    private String auditor;
}
