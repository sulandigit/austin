package com.java3y.austin.web.service;

import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.web.vo.AuditRecordVo;
import com.java3y.austin.web.vo.AuditTemplateParam;
import com.java3y.austin.web.vo.BatchAuditParam;
import com.java3y.austin.web.vo.BatchAuditResultVo;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 审批服务接口
 *
 * @author austin
 */
public interface AuditService {

    /**
     * 审批模板
     *
     * @param param 审批参数
     * @return 审批结果
     */
    BasicResultVO auditTemplate(AuditTemplateParam param);

    /**
     * 批量审批模板
     *
     * @param param 批量审批参数
     * @return 批量审批结果
     */
    BatchAuditResultVo batchAudit(BatchAuditParam param);

    /**
     * 查询待审批模板列表
     *
     * @param pageNum     页码
     * @param pageSize    每页数量
     * @param sendChannel 发送渠道
     * @param creator     创建者
     * @return 待审批模板列表
     */
    Page queryPendingAuditList(Integer pageNum, Integer pageSize, Integer sendChannel, String creator);

    /**
     * 查询审批历史
     *
     * @param templateId 模板ID
     * @return 审批历史列表
     */
    List<AuditRecordVo> queryAuditHistory(Long templateId);
}
