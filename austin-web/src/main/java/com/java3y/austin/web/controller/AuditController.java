package com.java3y.austin.web.controller;

import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.support.domain.MessageTemplate;
import com.java3y.austin.web.annotation.AustinAspect;
import com.java3y.austin.web.annotation.AustinResult;
import com.java3y.austin.web.service.AuditService;
import com.java3y.austin.web.utils.Convert4Amis;
import com.java3y.austin.web.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 审批管理Controller
 *
 * @author austin
 */
@Slf4j
@AustinAspect
@AustinResult
@RestController
@RequestMapping("/audit")
@Api("审批管理")
public class AuditController {

    @Autowired
    private AuditService auditService;

    /**
     * 审批模板
     */
    @PostMapping("/approve")
    @ApiOperation("/审批模板")
    public BasicResultVO auditTemplate(@Validated @RequestBody AuditTemplateParam param) {
        log.info("审批请求, 模板ID:{}, 审批人:{}, 审批结果:{}", param.getTemplateId(), param.getAuditor(), param.getAuditStatus());
        return auditService.auditTemplate(param);
    }

    /**
     * 批量审批模板
     */
    @PostMapping("/batchApprove")
    @ApiOperation("/批量审批模板")
    public BatchAuditResultVo batchAudit(@Validated @RequestBody BatchAuditParam param) {
        log.info("批量审批请求, 模板数量:{}, 审批人:{}, 审批结果:{}", param.getTemplateIds().size(), param.getAuditor(), param.getAuditStatus());
        return auditService.batchAudit(param);
    }

    /**
     * 查询待审批模板列表
     */
    @GetMapping("/pending")
    @ApiOperation("/查询待审批模板列表")
    public MessageTemplateVo queryPendingAuditList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sendChannel", required = false) Integer sendChannel,
            @RequestParam(value = "creator", required = false) String creator) {

        log.info("查询待审批列表, 页码:{}, 每页数量:{}", pageNum, pageSize);
        Page<MessageTemplate> page = auditService.queryPendingAuditList(pageNum, pageSize, sendChannel, creator);

        List<Map<String, Object>> result = Convert4Amis.flatListMap(page.toList());
        return MessageTemplateVo.builder()
                .count(page.getTotalElements())
                .rows(result)
                .build();
    }

    /**
     * 查询审批历史
     */
    @GetMapping("/history/{templateId}")
    @ApiOperation("/查询审批历史")
    public List<AuditRecordVo> queryAuditHistory(@PathVariable("templateId") Long templateId) {
        log.info("查询审批历史, 模板ID:{}", templateId);
        return auditService.queryAuditHistory(templateId);
    }
}
