package com.java3y.austin.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 审计日志执行结果枚举
 *
 * @author austin
 */
@Getter
@ToString
@AllArgsConstructor
public enum AuditResultEnum implements PowerfulEnum {

    /**
     * 10.执行成功
     */
    SUCCESS(10, "执行成功"),
    /**
     * 20.执行失败
     */
    FAILURE(20, "执行失败");

    private final Integer code;
    private final String description;

}
