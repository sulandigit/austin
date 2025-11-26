package com.java3y.austin.web.controller;

import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.web.exception.CommonException;
import com.java3y.austin.web.service.MessageSourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 国际化功能测试接口
 *
 * @author austin
 */
@Slf4j
@RestController
@RequestMapping("/i18n")
@Api("国际化测试")
public class I18nTestController {

    @Autowired
    private MessageSourceService messageSourceService;

    /**
     * 测试成功响应的国际化
     */
    @GetMapping("/test/success")
    @ApiOperation("测试成功响应")
    public BasicResultVO<Map<String, String>> testSuccess() {
        Map<String, String> data = new HashMap<>();
        data.put("message", messageSourceService.getMessage(RespStatusEnum.SUCCESS));
        data.put("description", "This endpoint tests successful response internationalization");
        return BasicResultVO.success(data);
    }

    /**
     * 测试失败响应的国际化
     */
    @GetMapping("/test/fail")
    @ApiOperation("测试失败响应")
    public BasicResultVO<String> testFail() {
        String message = messageSourceService.getMessage(RespStatusEnum.FAIL);
        return BasicResultVO.fail(message);
    }

    /**
     * 测试异常的国际化
     */
    @GetMapping("/test/exception")
    @ApiOperation("测试异常国际化")
    public BasicResultVO<String> testException() {
        // 这将触发ExceptionHandlerAdvice，返回国际化的错误消息
        throw new CommonException(RespStatusEnum.CLIENT_BAD_PARAMETERS);
    }

    /**
     * 测试未登录错误的国际化
     */
    @GetMapping("/test/no-login")
    @ApiOperation("测试未登录错误")
    public BasicResultVO<String> testNoLogin() {
        throw new CommonException(RespStatusEnum.NO_LOGIN);
    }

    /**
     * 测试模板不存在错误的国际化
     */
    @GetMapping("/test/template-not-found")
    @ApiOperation("测试模板不存在错误")
    public BasicResultVO<String> testTemplateNotFound() {
        throw new CommonException(RespStatusEnum.TEMPLATE_NOT_FOUND);
    }

    /**
     * 获取所有支持的错误消息（用于验证）
     */
    @GetMapping("/test/all-messages")
    @ApiOperation("获取所有错误消息")
    public BasicResultVO<Map<String, String>> getAllMessages() {
        Map<String, String> messages = new HashMap<>();
        
        // 响应消息
        messages.put("SUCCESS", messageSourceService.getMessage(RespStatusEnum.SUCCESS));
        messages.put("FAIL", messageSourceService.getMessage(RespStatusEnum.FAIL));
        
        // 错误消息
        messages.put("ERROR_500", messageSourceService.getMessage(RespStatusEnum.ERROR_500));
        messages.put("ERROR_400", messageSourceService.getMessage(RespStatusEnum.ERROR_400));
        messages.put("CLIENT_BAD_PARAMETERS", messageSourceService.getMessage(RespStatusEnum.CLIENT_BAD_PARAMETERS));
        messages.put("TEMPLATE_NOT_FOUND", messageSourceService.getMessage(RespStatusEnum.TEMPLATE_NOT_FOUND));
        messages.put("TOO_MANY_RECEIVER", messageSourceService.getMessage(RespStatusEnum.TOO_MANY_RECEIVER));
        messages.put("NO_LOGIN", messageSourceService.getMessage(RespStatusEnum.NO_LOGIN));
        messages.put("SERVICE_ERROR", messageSourceService.getMessage(RespStatusEnum.SERVICE_ERROR));
        messages.put("RESOURCE_NOT_FOUND", messageSourceService.getMessage(RespStatusEnum.RESOURCE_NOT_FOUND));
        
        // Pipeline错误
        messages.put("CONTEXT_IS_NULL", messageSourceService.getMessage(RespStatusEnum.CONTEXT_IS_NULL));
        messages.put("BUSINESS_CODE_IS_NULL", messageSourceService.getMessage(RespStatusEnum.BUSINESS_CODE_IS_NULL));
        messages.put("PROCESS_TEMPLATE_IS_NULL", messageSourceService.getMessage(RespStatusEnum.PROCESS_TEMPLATE_IS_NULL));
        messages.put("PROCESS_LIST_IS_NULL", messageSourceService.getMessage(RespStatusEnum.PROCESS_LIST_IS_NULL));
        
        return BasicResultVO.success(messages);
    }
}
