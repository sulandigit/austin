package com.java3y.austin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Template enumeration information
 * 模板枚举信息
 *
 * @author 3y
 */
@Getter
@ToString
@AllArgsConstructor
public enum TemplateType implements PowerfulEnum {

    /**
     * Scheduled template (called by backend scheduler)
     * 定时类的模板(后台定时调用)
     */
    CLOCKING(10, "定时类的模板(后台定时调用)"),
    /**
     * Real-time template (called by API in real-time)
     * 实时类的模板(接口实时调用)
     */
    REALTIME(20, "实时类的模板(接口实时调用)"),
    ;

    private final Integer code;
    private final String description;

}
