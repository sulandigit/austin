package com.java3y.austin.support.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.java3y.austin.support.dao.OperationAuditLogDao;
import com.java3y.austin.support.domain.OperationAuditLog;
import com.java3y.austin.support.dto.AuditLogQueryParam;
import com.java3y.austin.support.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 审计日志业务实现类
 *
 * @author austin
 */
@Slf4j
@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private OperationAuditLogDao operationAuditLogDao;

    /**
     * 异步保存审计日志
     *
     * @param auditLog 审计日志对象
     */
    @Override
    @Async("auditLogExecutor")
    public void saveAuditLog(OperationAuditLog auditLog) {
        try {
            operationAuditLogDao.save(auditLog);
            log.debug("审计日志保存成功，操作人：{}，URI：{}", auditLog.getOperator(), auditLog.getRequestUri());
        } catch (Exception e) {
            // 审计日志保存失败不影响主业务，仅记录日志
            log.error("审计日志保存失败，操作人：{}，URI：{}，异常：{}", 
                    auditLog.getOperator(), auditLog.getRequestUri(), e.getMessage(), e);
        }
    }

    /**
     * 多条件分页查询审计日志
     *
     * @param queryParam 查询参数
     * @return 分页结果
     */
    @Override
    public Page<OperationAuditLog> queryAuditLogs(AuditLogQueryParam queryParam) {
        // 构建分页对象
        Pageable pageable = buildPageable(queryParam);

        // 构建查询条件
        Specification<OperationAuditLog> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 操作人模糊查询
            if (CharSequenceUtil.isNotBlank(queryParam.getOperator())) {
                predicates.add(criteriaBuilder.like(root.get("operator"), "%" + queryParam.getOperator() + "%"));
            }

            // 时间范围查询
            if (Objects.nonNull(queryParam.getStartTime())) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("operateTime"), queryParam.getStartTime()));
            }
            if (Objects.nonNull(queryParam.getEndTime())) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("operateTime"), queryParam.getEndTime()));
            }

            // URI模糊查询
            if (CharSequenceUtil.isNotBlank(queryParam.getRequestUri())) {
                predicates.add(criteriaBuilder.like(root.get("requestUri"), "%" + queryParam.getRequestUri() + "%"));
            }

            // 执行结果查询
            if (Objects.nonNull(queryParam.getExecuteResult())) {
                predicates.add(criteriaBuilder.equal(root.get("executeResult"), queryParam.getExecuteResult()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return operationAuditLogDao.findAll(specification, pageable);
    }

    /**
     * 导出审计日志
     *
     * @param queryParam 查询参数
     * @return 审计日志列表
     */
    @Override
    public List<OperationAuditLog> exportAuditLogs(AuditLogQueryParam queryParam) {
        // 构建查询条件
        Specification<OperationAuditLog> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (CharSequenceUtil.isNotBlank(queryParam.getOperator())) {
                predicates.add(criteriaBuilder.like(root.get("operator"), "%" + queryParam.getOperator() + "%"));
            }

            if (Objects.nonNull(queryParam.getStartTime())) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("operateTime"), queryParam.getStartTime()));
            }
            if (Objects.nonNull(queryParam.getEndTime())) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("operateTime"), queryParam.getEndTime()));
            }

            if (CharSequenceUtil.isNotBlank(queryParam.getRequestUri())) {
                predicates.add(criteriaBuilder.like(root.get("requestUri"), "%" + queryParam.getRequestUri() + "%"));
            }

            if (Objects.nonNull(queryParam.getExecuteResult())) {
                predicates.add(criteriaBuilder.equal(root.get("executeResult"), queryParam.getExecuteResult()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 按操作时间倒序排序
        Sort sort = Sort.by(Sort.Direction.DESC, "operateTime");
        return operationAuditLogDao.findAll(specification, sort);
    }

    /**
     * 构建分页对象
     *
     * @param queryParam 查询参数
     * @return 分页对象
     */
    private Pageable buildPageable(AuditLogQueryParam queryParam) {
        int pageNum = Objects.nonNull(queryParam.getPageNum()) ? queryParam.getPageNum() : 0;
        int pageSize = Objects.nonNull(queryParam.getPageSize()) ? queryParam.getPageSize() : 10;
        
        // 按操作时间倒序排序
        Sort sort = Sort.by(Sort.Direction.DESC, "operateTime");
        return PageRequest.of(pageNum, pageSize, sort);
    }

}
