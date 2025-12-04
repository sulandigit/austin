package com.java3y.austin.web.exception;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.support.constans.SentinelConstant;
import org.assertj.core.util.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author kl
 * @version 1.0.0
 * @description 拦截异常统一返回
 * @date 2023/2/9 19:03
 */
@ControllerAdvice(basePackages = "com.java3y.austin.web.controller")
@ResponseBody
public class ExceptionHandlerAdvice {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);


    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.OK)
    public BasicResultVO<String> exceptionResponse(Exception e) {
        String errStackStr = Throwables.getStackTrace(e);
        log.error(errStackStr);
        return BasicResultVO.fail(RespStatusEnum.ERROR_500, "\r\n" + errStackStr + "\r\n");
    }

    @ExceptionHandler({CommonException.class})
    @ResponseStatus(HttpStatus.OK)
    public BasicResultVO<RespStatusEnum> commonResponse(CommonException ce) {
        log.error(Throwables.getStackTrace(ce));
        return new BasicResultVO<>(ce.getCode(), ce.getMessage(), ce.getRespStatusEnum());
    }

    /**
     * Sentinel 限流异常处理
     */
    @ExceptionHandler({FlowException.class})
    @ResponseStatus(HttpStatus.OK)
    public BasicResultVO<String> handleFlowException(FlowException e) {
        log.warn("[Sentinel] Flow control triggered: resource={}, rule={}", e.getRule().getResource(), e.getRule());
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, SentinelConstant.FALLBACK_MSG_FLOW_CONTROL);
    }

    /**
     * Sentinel 熔断降级异常处理
     */
    @ExceptionHandler({DegradeException.class})
    @ResponseStatus(HttpStatus.OK)
    public BasicResultVO<String> handleDegradeException(DegradeException e) {
        log.warn("[Sentinel] Degrade triggered: resource={}, rule={}", e.getRule().getResource(), e.getRule());
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, SentinelConstant.FALLBACK_MSG_DEGRADE);
    }

    /**
     * Sentinel 系统保护异常处理
     */
    @ExceptionHandler({SystemBlockException.class})
    @ResponseStatus(HttpStatus.OK)
    public BasicResultVO<String> handleSystemBlockException(SystemBlockException e) {
        log.warn("[Sentinel] System protection triggered: {}", e.getMessage());
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, SentinelConstant.FALLBACK_MSG_SYSTEM_BLOCK);
    }

    /**
     * Sentinel 通用 Block 异常处理
     */
    @ExceptionHandler({BlockException.class})
    @ResponseStatus(HttpStatus.OK)
    public BasicResultVO<String> handleBlockException(BlockException e) {
        log.warn("[Sentinel] Block exception: resource={}, rule={}", e.getRule().getResource(), e.getMessage());
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, SentinelConstant.FALLBACK_MSG_FLOW_CONTROL);
    }
}

