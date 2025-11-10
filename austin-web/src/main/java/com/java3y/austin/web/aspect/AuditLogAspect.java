package com.java3y.austin.web.aspect;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.java3y.austin.common.enums.AuditResultEnum;
import com.java3y.austin.support.domain.OperationAuditLog;
import com.java3y.austin.support.service.AuditLogService;
import com.java3y.austin.web.annotation.AuditLog;
import com.java3y.austin.web.utils.LoginUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 审计日志切面处理器
 * 拦截标注了@AustinAspect的Controller方法，收集审计信息并异步保存
 *
 * @author austin
 */
@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired(required = false)
    private LoginUtils loginUtils;

    /**
     * 审计日志功能开关
     */
    @Value("${austin.audit.enabled:true}")
    private Boolean auditEnabled;

    /**
     * 是否记录请求参数
     */
    @Value("${austin.audit.record-params:true}")
    private Boolean recordParams;

    /**
     * 请求参数最大长度
     */
    @Value("${austin.audit.max-param-length:5000}")
    private Integer maxParamLength;

    /**
     * 切点定义：拦截所有标注了@AustinAspect的类或方法
     */
    @Pointcut("@within(com.java3y.austin.web.annotation.AustinAspect) || @annotation(com.java3y.austin.web.annotation.AustinAspect)")
    public void auditPointcut() {
    }

    /**
     * 环绕通知：记录审计日志
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("auditPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 如果审计功能未开启，直接执行方法
        if (!auditEnabled) {
            return joinPoint.proceed();
        }

        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 获取方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        
        // 构建审计日志对象
        OperationAuditLog auditLog = new OperationAuditLog();
        
        // 设置基础信息
        auditLog.setOperateTime(new Date());
        auditLog.setOperateIp(getClientIp());
        auditLog.setRequestUri(request.getRequestURI());
        auditLog.setRequestMethod(request.getMethod());
        auditLog.setUserAgent(request.getHeader("user-agent"));
        auditLog.setCreated((int) (System.currentTimeMillis() / 1000));
        
        // 设置操作人
        auditLog.setOperator(getOperator(joinPoint.getArgs()));
        
        // 设置操作类型
        auditLog.setOperationType(getOperationType(method));
        
        // 设置请求参数
        if (recordParams && shouldRecordParams(method)) {
            auditLog.setRequestParams(getRequestParams(joinPoint.getArgs()));
        }
        
        // 执行方法
        Object result = null;
        try {
            result = joinPoint.proceed();
            
            // 执行成功
            auditLog.setExecuteResult(AuditResultEnum.SUCCESS.getCode());
            
        } catch (Throwable throwable) {
            // 执行失败
            auditLog.setExecuteResult(AuditResultEnum.FAILURE.getCode());
            auditLog.setErrorMessage(getErrorMessage(throwable));
            
            // 继续抛出异常
            throw throwable;
            
        } finally {
            // 计算执行耗时
            long endTime = System.currentTimeMillis();
            auditLog.setExecuteDuration((int) (endTime - startTime));
            
            // 异步保存审计日志
            try {
                auditLogService.saveAuditLog(auditLog);
            } catch (Exception e) {
                // 审计日志保存失败不影响主业务，仅记录错误日志
                log.error("审计日志异步保存失败，URI: {}, 异常: {}", 
                        auditLog.getRequestUri(), e.getMessage());
            }
        }
        
        return result;
    }

    /**
     * 获取操作人
     *
     * @param args 方法参数
     * @return 操作人
     */
    private String getOperator(Object[] args) {
        // 优先从LoginUtils获取登录用户
        if (Objects.nonNull(loginUtils)) {
            try {
                // 这里可以扩展获取登录用户的逻辑
                // 当前项目使用微信服务号登录，暂时从请求参数中提取
            } catch (Exception e) {
                log.debug("获取登录用户失败: {}", e.getMessage());
            }
        }
        
        // 从请求参数中提取creator或updator字段
        for (Object arg : args) {
            if (Objects.isNull(arg)) {
                continue;
            }
            try {
                String json = JSON.toJSONString(arg);
                if (json.contains("\"creator\"")) {
                    String creator = JSON.parseObject(json).getString("creator");
                    if (CharSequenceUtil.isNotBlank(creator)) {
                        return creator;
                    }
                }
                if (json.contains("\"updator\"")) {
                    String updator = JSON.parseObject(json).getString("updator");
                    if (CharSequenceUtil.isNotBlank(updator)) {
                        return updator;
                    }
                }
            } catch (Exception e) {
                // 忽略解析异常
            }
        }
        
        // 默认返回匿名用户
        return "匿名用户";
    }

    /**
     * 获取操作类型
     *
     * @param method 方法
     * @return 操作类型
     */
    private String getOperationType(Method method) {
        // 优先读取@AuditLog注解的operationType
        if (method.isAnnotationPresent(AuditLog.class)) {
            AuditLog auditLog = method.getAnnotation(AuditLog.class);
            if (CharSequenceUtil.isNotBlank(auditLog.operationType())) {
                return auditLog.operationType();
            }
        }
        
        // 读取@ApiOperation注解的value
        if (method.isAnnotationPresent(ApiOperation.class)) {
            ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
            if (CharSequenceUtil.isNotBlank(apiOperation.value())) {
                return apiOperation.value();
            }
        }
        
        // 根据RequestMapping路径和方法名生成
        String httpMethod = request.getMethod();
        String uri = request.getRequestURI();
        return httpMethod + " " + uri;
    }

    /**
     * 判断是否应该记录请求参数
     *
     * @param method 方法
     * @return 是否记录
     */
    private boolean shouldRecordParams(Method method) {
        if (method.isAnnotationPresent(AuditLog.class)) {
            AuditLog auditLog = method.getAnnotation(AuditLog.class);
            return auditLog.recordParams();
        }
        return true;
    }

    /**
     * 获取请求参数（JSON格式）
     *
     * @param args 方法参数
     * @return JSON字符串
     */
    private String getRequestParams(Object[] args) {
        List<Object> params = Lists.newArrayList();
        
        // 过滤掉无法序列化的对象
        Arrays.stream(args).forEach(arg -> {
            if (arg instanceof MultipartFile || arg instanceof HttpServletRequest
                    || arg instanceof HttpServletResponse || arg instanceof BindingResult) {
                return;
            }
            params.add(arg);
        });
        
        try {
            String jsonParams = JSON.toJSONString(params);
            
            // 限制参数长度
            if (jsonParams.length() > maxParamLength) {
                jsonParams = jsonParams.substring(0, maxParamLength) + "...(截断)";
            }
            
            // 脱敏处理
            jsonParams = desensitize(jsonParams);
            
            return jsonParams;
        } catch (Exception e) {
            log.warn("请求参数序列化失败: {}", e.getMessage());
            return "[参数序列化失败]";
        }
    }

    /**
     * 数据脱敏
     *
     * @param jsonParams JSON参数
     * @return 脱敏后的参数
     */
    private String desensitize(String jsonParams) {
        // 密码类字段脱敏
        jsonParams = jsonParams.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"******\"");
        jsonParams = jsonParams.replaceAll("\"passwd\"\\s*:\\s*\"[^\"]*\"", "\"passwd\":\"******\"");
        
        // Token类字段脱敏（保留前4位和后4位）
        // 这里简化处理，完整实现可以使用正则提取token值后脱敏
        
        return jsonParams;
    }

    /**
     * 获取错误信息
     *
     * @param throwable 异常
     * @return 错误信息
     */
    private String getErrorMessage(Throwable throwable) {
        String stackTrace = Throwables.getStackTraceAsString(throwable);
        
        // 限制错误信息长度为500字符
        if (stackTrace.length() > 500) {
            stackTrace = stackTrace.substring(0, 500) + "...(截断)";
        }
        
        return stackTrace;
    }

    /**
     * 获取客户端IP地址
     *
     * @return IP地址
     */
    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (CharSequenceUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            }
            return ip;
        }
        
        ip = request.getHeader("X-Real-IP");
        if (CharSequenceUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        return request.getRemoteAddr();
    }

}
