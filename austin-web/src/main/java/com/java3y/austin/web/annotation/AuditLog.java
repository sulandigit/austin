package com.java3y.austin.web.annotation;

import java.lang.annotation.*;

/**
 * 审计日志注解
 * 用于标记需要审计的方法，支持自定义操作类型描述
 *
 * @author austin
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 操作类型描述（如"保存消息模板"、"删除渠道账号"）
     * 如果不指定，系统将根据方法名和RequestMapping路径自动生成
     */
    String operationType() default "";

    /**
     * 是否记录请求参数
     */
    boolean recordParams() default true;

}
