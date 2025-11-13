package com.java3y.austin.web.service;

import com.java3y.austin.common.enums.RespStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 国际化消息服务
 * 提供统一的消息获取接口，封装MessageSource的调用细节
 *
 * @author austin
 */
@Service
public class MessageSourceService {

    @Autowired
    private MessageSource messageSource;

    /**
     * 获取国际化消息（使用当前线程的Locale）
     *
     * @param code 消息键
     * @return 国际化消息文本
     */
    public String getMessage(String code) {
        return getMessage(code, null);
    }

    /**
     * 获取国际化消息，支持占位符参数（使用当前线程的Locale）
     *
     * @param code 消息键
     * @param args 占位符参数
     * @return 参数化国际化消息
     */
    public String getMessage(String code, Object... args) {
        return getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * 获取国际化消息，指定Locale
     *
     * @param code   消息键
     * @param args   占位符参数
     * @param locale 语言环境
     * @return 国际化消息文本
     */
    public String getMessage(String code, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (Exception e) {
            // 如果找不到消息，返回消息键本身
            return code;
        }
    }

    /**
     * 从响应状态枚举获取国际化消息
     *
     * @param status 响应状态枚举
     * @return 国际化消息文本
     */
    public String getMessage(RespStatusEnum status) {
        if (status == null) {
            return "";
        }
        // 根据枚举名称构建消息键
        String messageKey = getMessageKey(status);
        return getMessage(messageKey);
    }

    /**
     * 从响应状态枚举获取国际化消息，支持占位符参数
     *
     * @param status 响应状态枚举
     * @param args   占位符参数
     * @return 参数化国际化消息
     */
    public String getMessage(RespStatusEnum status, Object... args) {
        if (status == null) {
            return "";
        }
        String messageKey = getMessageKey(status);
        return getMessage(messageKey, args);
    }

    /**
     * 根据响应状态枚举构建消息键
     *
     * @param status 响应状态枚举
     * @return 消息键
     */
    private String getMessageKey(RespStatusEnum status) {
        String enumName = status.name();
        // SUCCESS 和 FAIL 使用 response.前缀
        if ("SUCCESS".equals(enumName) || "FAIL".equals(enumName)) {
            return "response." + enumName;
        }
        // 其他使用 error.前缀
        return "error." + enumName;
    }
}
