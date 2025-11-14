package com.java3y.austin.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量审批结果VO
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAuditResultVo {

    /**
     * 成功审批数量
     */
    private Integer successCount;

    /**
     * 失败审批数量
     */
    private Integer failCount;

    /**
     * 失败详情列表
     */
    private List<FailDetail> failDetails;

    /**
     * 失败详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailDetail {
        /**
         * 模板ID
         */
        private Long templateId;

        /**
         * 失败原因
         */
        private String reason;
    }
}
