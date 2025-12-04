package com.java3y.austin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 告警级别枚举
 *
 * @author austin
 */
@Getter
@ToString
@AllArgsConstructor
public enum AlertLevel implements PowerfulEnum {
    /**
     * 信息级别
     */
    INFO(10, "INFO"),
    /**
     * 警告级别
     */
    WARN(20, "WARN"),
    /**
     * 严重级别
     */
    CRITICAL(30, "CRITICAL");

    private final Integer code;
    private final String description;
}
