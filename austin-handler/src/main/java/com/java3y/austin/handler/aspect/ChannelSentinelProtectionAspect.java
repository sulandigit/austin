package com.java3y.austin.handler.aspect;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.java3y.austin.handler.annotation.ChannelSentinelProtection;
import com.java3y.austin.support.constans.SentinelConstant;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 渠道调用 Sentinel 保护切面
 * <p>
 * 功能：
 * 1. 拦截带有 @ChannelSentinelProtection 注解的方法
 * 2. 为每个渠道调用创建独立的 Sentinel 资源
 * 3. 在发生熔断或限流时，调用降级方法或抛出异常
 *
 * @author austin
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "austin.sentinel.enabled", havingValue = "true", matchIfMissing = false)
public class ChannelSentinelProtectionAspect {

    /**
     * 环绕通知：处理渠道调用的 Sentinel 保护
     */
    @Around("@annotation(com.java3y.austin.handler.annotation.ChannelSentinelProtection)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ChannelSentinelProtection annotation = method.getAnnotation(ChannelSentinelProtection.class);

        if (annotation == null) {
            return joinPoint.proceed();
        }

        // 构建资源名称：downstream:{channelType}:{supplier}
        String channelType = annotation.channelType();
        String supplier = annotation.supplier();
        String resourceName = buildResourceName(channelType, supplier);

        Entry entry = null;
        try {
            // 尝试进入 Sentinel 保护资源
            entry = SphU.entry(resourceName);
            
            // 执行原方法
            return joinPoint.proceed();
            
        } catch (BlockException e) {
            // Sentinel 限流或熔断
            log.warn("[ChannelSentinelProtection] Resource blocked: {}, rule: {}", 
                    resourceName, e.getRule());
            
            // 如果配置了降级方法，则调用降级方法
            if (annotation.enableDegrade() && !annotation.fallbackMethod().isEmpty()) {
                return invokeFallbackMethod(joinPoint, annotation.fallbackMethod());
            }
            
            // 否则抛出异常，由上层处理
            throw e;
            
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    /**
     * 构建资源名称
     */
    private String buildResourceName(String channelType, String supplier) {
        return SentinelConstant.RESOURCE_PREFIX_DOWNSTREAM + channelType + ":" + supplier;
    }

    /**
     * 调用降级方法
     */
    private Object invokeFallbackMethod(ProceedingJoinPoint joinPoint, String fallbackMethodName) {
        try {
            Object target = joinPoint.getTarget();
            Method fallbackMethod = target.getClass().getMethod(
                    fallbackMethodName, 
                    ((MethodSignature) joinPoint.getSignature()).getParameterTypes()
            );
            fallbackMethod.setAccessible(true);
            return fallbackMethod.invoke(target, joinPoint.getArgs());
        } catch (Exception e) {
            log.error("[ChannelSentinelProtection] Failed to invoke fallback method: {}", 
                    fallbackMethodName, e);
            return null;
        }
    }
}
