package com.java3y.austin.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Anchor point information
 * 埋点信息
 *
 * @author 3y
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnchorInfo {
    /**
     * Unique message ID (used for data tracking)
     * 消息唯一Id(数据追踪使用)
     * Generation logic reference: TaskInfoUtils
     * 生成逻辑参考 TaskInfoUtils
     */
    private String bizId;

    /**
     * Unique message ID (used for data tracking)
     * 消息唯一Id(数据追踪使用)
     * Generation logic reference: TaskInfoUtils
     * 生成逻辑参考 TaskInfoUtils
     */
    private String messageId;
    /**
     * Sending users
     * 发送用户
     */
    private Set<String> ids;

    /**
     * Specific anchor state
     * 具体点位
     *
     * @see com.java3y.austin.common.enums.AnchorState
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
     * Log generation timestamp
     * 日志生成时间
     */
    private long logTimestamp;

}
