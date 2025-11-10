package com.java3y.austin.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 审计日志展示对象
 *
 * @author austin
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogVo {

    /**
     * 返回List列表
     */
    private List<Map<String, Object>> rows;

    /**
     * 总条数
     */
    private Long count;

}
