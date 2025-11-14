package com.java3y.austin.web.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.java3y.austin.common.enums.AuditStatus;
import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.support.dao.AuditRecordDao;
import com.java3y.austin.support.dao.MessageTemplateDao;
import com.java3y.austin.support.domain.AuditRecord;
import com.java3y.austin.support.domain.MessageTemplate;
import com.java3y.austin.web.service.AuditService;
import com.java3y.austin.web.vo.AuditRecordVo;
import com.java3y.austin.web.vo.AuditTemplateParam;
import com.java3y.austin.web.vo.BatchAuditParam;
import com.java3y.austin.web.vo.BatchAuditResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 审批服务实现类
 *
 * @author austin
 */
@Slf4j
@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private MessageTemplateDao messageTemplateDao;

    @Autowired
    private AuditRecordDao auditRecordDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BasicResultVO auditTemplate(AuditTemplateParam param) {
        try {
            // 1. 参数校验
            if (param.getAuditOpinion() != null && param.getAuditOpinion().length() > 500) {
                return BasicResultVO.fail(RespStatusEnum.CLIENT_BAD_PARAMETERS, "审批意见最多500字符");
            }

            // 2. 校验审批状态
            if (!AuditStatus.AUDIT_SUCCESS.getCode().equals(param.getAuditStatus())
                    && !AuditStatus.AUDIT_REJECT.getCode().equals(param.getAuditStatus())) {
                return BasicResultVO.fail(RespStatusEnum.CLIENT_BAD_PARAMETERS, "审批状态不合法");
            }

            // 3. 查询模板
            Optional<MessageTemplate> optionalTemplate = messageTemplateDao.findById(param.getTemplateId());
            if (!optionalTemplate.isPresent()) {
                return BasicResultVO.fail(RespStatusEnum.TEMPLATE_NOT_FOUND);
            }

            MessageTemplate template = optionalTemplate.get();

            // 4. 检查当前状态是否为"待审核"
            if (!AuditStatus.WAIT_AUDIT.getCode().equals(template.getAuditStatus())) {
                return BasicResultVO.fail(RespStatusEnum.CLIENT_BAD_PARAMETERS, "当前模板状态不允许审批");
            }

            // 5. 更新模板审批状态
            template.setAuditStatus(param.getAuditStatus());
            template.setAuditor(param.getAuditor());
            template.setUpdated(Math.toIntExact(DateUtil.currentSeconds()));
            messageTemplateDao.save(template);

            // 6. 创建审批历史记录
            AuditRecord auditRecord = createAuditRecord(template, param);
            auditRecordDao.save(auditRecord);

            log.info("审批完成, 模板ID:{}, 审批人:{}, 审批结果:{}", param.getTemplateId(), param.getAuditor(), param.getAuditStatus());

            return BasicResultVO.success();
        } catch (Exception e) {
            log.error("审批失败, 模板ID:{}, 错误信息:{}", param.getTemplateId(), e.getMessage(), e);
            return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, "审批失败:" + e.getMessage());
        }
    }

    @Override
    public BatchAuditResultVo batchAudit(BatchAuditParam param) {
        List<BatchAuditResultVo.FailDetail> failDetails = new ArrayList<>();
        int successCount = 0;

        for (Long templateId : param.getTemplateIds()) {
            try {
                AuditTemplateParam auditParam = AuditTemplateParam.builder()
                        .templateId(templateId)
                        .auditStatus(param.getAuditStatus())
                        .auditOpinion(param.getAuditOpinion())
                        .auditor(param.getAuditor())
                        .build();

                BasicResultVO result = auditTemplate(auditParam);
                if (RespStatusEnum.SUCCESS.getCode().equals(result.getStatus())) {
                    successCount++;
                } else {
                    failDetails.add(BatchAuditResultVo.FailDetail.builder()
                            .templateId(templateId)
                            .reason(result.getMsg())
                            .build());
                }
            } catch (Exception e) {
                log.error("批量审批失败, 模板ID:{}, 错误信息:{}", templateId, e.getMessage(), e);
                failDetails.add(BatchAuditResultVo.FailDetail.builder()
                        .templateId(templateId)
                        .reason("审批异常:" + e.getMessage())
                        .build());
            }
        }

        return BatchAuditResultVo.builder()
                .successCount(successCount)
                .failCount(failDetails.size())
                .failDetails(failDetails)
                .build();
    }

    @Override
    public Page queryPendingAuditList(Integer pageNum, Integer pageSize, Integer sendChannel, String creator) {
        // 构建查询条件
        Specification<MessageTemplate> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 审批状态为待审核
            predicates.add(cb.equal(root.get("auditStatus"), AuditStatus.WAIT_AUDIT.getCode()));

            // 未删除
            predicates.add(cb.equal(root.get("isDeleted"), 0));

            // 发送渠道过滤
            if (Objects.nonNull(sendChannel)) {
                predicates.add(cb.equal(root.get("sendChannel"), sendChannel));
            }

            // 创建者过滤
            if (StrUtil.isNotBlank(creator)) {
                predicates.add(cb.equal(root.get("creator"), creator));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 分页查询
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
        return messageTemplateDao.findAll(specification, pageRequest);
    }

    @Override
    public List<AuditRecordVo> queryAuditHistory(Long templateId) {
        List<AuditRecord> records = auditRecordDao.findByTemplateIdOrderByCreatedDesc(templateId);

        return records.stream().map(record -> {
            AuditRecordVo vo = AuditRecordVo.builder()
                    .id(record.getId())
                    .templateId(record.getTemplateId())
                    .auditStatus(record.getAuditStatus())
                    .auditStatusDesc(getAuditStatusDesc(record.getAuditStatus()))
                    .auditor(record.getAuditor())
                    .auditOpinion(record.getAuditOpinion())
                    .auditTime(record.getAuditTime())
                    .templateSnapshot(record.getTemplateSnapshot())
                    .created(record.getCreated())
                    .build();
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 创建审批记录
     *
     * @param template 消息模板
     * @param param    审批参数
     * @return 审批记录
     */
    private AuditRecord createAuditRecord(MessageTemplate template, AuditTemplateParam param) {
        // 生成模板快照
        String snapshot = JSON.toJSONString(template);

        int currentTime = Math.toIntExact(DateUtil.currentSeconds());

        return AuditRecord.builder()
                .templateId(template.getId())
                .auditStatus(param.getAuditStatus())
                .auditor(param.getAuditor())
                .auditOpinion(param.getAuditOpinion() == null ? "" : param.getAuditOpinion())
                .auditTime(currentTime)
                .templateSnapshot(snapshot)
                .created(currentTime)
                .build();
    }

    /**
     * 获取审批状态描述
     *
     * @param auditStatus 审批状态
     * @return 状态描述
     */
    private String getAuditStatusDesc(Integer auditStatus) {
        if (AuditStatus.WAIT_AUDIT.getCode().equals(auditStatus)) {
            return AuditStatus.WAIT_AUDIT.getDescription();
        } else if (AuditStatus.AUDIT_SUCCESS.getCode().equals(auditStatus)) {
            return AuditStatus.AUDIT_SUCCESS.getDescription();
        } else if (AuditStatus.AUDIT_REJECT.getCode().equals(auditStatus)) {
            return AuditStatus.AUDIT_REJECT.getDescription();
        }
        return "未知状态";
    }
}
