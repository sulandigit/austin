package com.java3y.austin.service.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Business code enum
 * @author 3y
 * @date 2021/11/22
 */
@Getter
@ToString
@AllArgsConstructor
public enum BusinessCode {

    /**
     * Common send process
     * 普通发送流程
     */
    COMMON_SEND("send", "普通发送"),

    /**
     * Recall process
     * 撤回流程
     */
    RECALL("recall", "撤回消息");


    /**
     * Code associated with the chain of responsibility template
     * code 关联着责任链的模板
     */
    private final String code;

    /**
     * Type description
     * 类型说明
     */
    private final String description;


}
