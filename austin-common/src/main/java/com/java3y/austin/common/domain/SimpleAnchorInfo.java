package com.java3y.austin.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple anchor point information
 * 简单的埋点信息
 *
 * @author 3y
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleAnchorInfo {


    /**
     * Specific anchor state
     * 具体点位
     */
    private int state;

    /**
     * Business ID (used for data tracking)
     * 业务Id(数据追踪使用)
     * Generation logic reference: TaskInfoUtils
     * 生成逻辑参考 TaskInfoUtils
     */
    private Long businessId;

    /**
     * Generation timestamp
     * 生成时间
     */
    private long timestamp;
}
