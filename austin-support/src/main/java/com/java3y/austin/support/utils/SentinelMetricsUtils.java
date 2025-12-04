package com.java3y.austin.support.utils;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel 监控指标工具类
 * <p>
 * 功能：
 * 1. 获取 Sentinel 资源的实时监控指标
 * 2. 提供统一的指标查询接口
 * 3. 便于集成到现有监控体系（Prometheus/Grafana）
 *
 * @author austin
 */
@Slf4j
public class SentinelMetricsUtils {

    /**
     * 获取资源的监控指标
     *
     * @param resourceName 资源名称
     * @return 指标 Map
     */
    public static Map<String, Object> getResourceMetrics(String resourceName) {
        Map<String, Object> metrics = new HashMap<>();

        try {
            ClusterNode node = ClusterBuilderSlot.getClusterNode(resourceName);
            if (node == null) {
                log.warn("[SentinelMetrics] Resource not found: {}", resourceName);
                return metrics;
            }

            // 通过 QPS
            metrics.put("passQps", node.passQps());
            
            // 成功 QPS
            metrics.put("successQps", node.successQps());
            
            // 异常 QPS
            metrics.put("exceptionQps", node.exceptionQps());
            
            // 阻塞 QPS
            metrics.put("blockQps", node.blockQps());
            
            // 平均响应时间（毫秒）
            metrics.put("avgRt", node.avgRt());
            
            // 当前线程数
            metrics.put("curThreadNum", node.curThreadNum());
            
            // 总请求数
            metrics.put("totalRequest", node.totalRequest());
            
            // 总成功数
            metrics.put("totalSuccess", node.totalSuccess());
            
            // 总异常数
            metrics.put("totalException", node.totalException());
            
            // 总阻塞数
            metrics.put("totalBlock", node.blockRequest());

        } catch (Exception e) {
            log.error("[SentinelMetrics] Failed to get metrics for resource: {}", resourceName, e);
        }

        return metrics;
    }

    /**
     * 记录 Sentinel Block 事件
     *
     * @param resourceName 资源名称
     * @param blockException Block 异常
     */
    public static void recordBlockEvent(String resourceName, BlockException blockException) {
        log.warn("[SentinelMetrics] Block event - resource: {}, rule: {}, ruleLimit: {}", 
                resourceName, 
                blockException.getRule(), 
                blockException.getRuleLimitApp());
        
        // 这里可以将 Block 事件上报到监控系统
        // 例如：发送到 Prometheus、Grafana、Graylog 等
    }

    /**
     * 记录 Sentinel Entry 成功事件
     *
     * @param resourceName 资源名称
     * @param rt 响应时间（毫秒）
     */
    public static void recordSuccessEvent(String resourceName, long rt) {
        if (log.isDebugEnabled()) {
            log.debug("[SentinelMetrics] Success event - resource: {}, rt: {}ms", resourceName, rt);
        }
        
        // 这里可以将成功事件上报到监控系统
    }

    /**
     * 记录 Sentinel 异常事件
     *
     * @param resourceName 资源名称
     * @param throwable 异常
     */
    public static void recordExceptionEvent(String resourceName, Throwable throwable) {
        log.error("[SentinelMetrics] Exception event - resource: {}, error: {}", 
                resourceName, 
                throwable.getMessage());
        
        // 这里可以将异常事件上报到监控系统
    }

    /**
     * 打印资源的监控指标（用于调试）
     *
     * @param resourceName 资源名称
     */
    public static void printResourceMetrics(String resourceName) {
        Map<String, Object> metrics = getResourceMetrics(resourceName);
        if (metrics.isEmpty()) {
            log.info("[SentinelMetrics] No metrics available for resource: {}", resourceName);
            return;
        }

        log.info("[SentinelMetrics] Resource: {} - Metrics: {}", resourceName, metrics);
    }

    /**
     * 获取当前上下文名称
     *
     * @return 上下文名称
     */
    public static String getCurrentContextName() {
        return ContextUtil.getContext() != null ? ContextUtil.getContext().getName() : "default";
    }
}
