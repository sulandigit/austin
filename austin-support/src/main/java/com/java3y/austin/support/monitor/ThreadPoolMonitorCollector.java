package com.java3y.austin.support.monitor;

import com.dtp.common.dto.DtpMainProp;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.core.DtpRegistry;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池监控指标收集器
 * 负责收集系统中所有线程池的运行指标数据
 *
 * @author austin
 */
@Slf4j
@Component
public class ThreadPoolMonitorCollector {

    /**
     * 收集所有线程池的监控指标
     *
     * @return 线程池监控指标列表
     */
    public List<ThreadPoolMonitorMetrics> collectAllMetrics() {
        List<ThreadPoolMonitorMetrics> metricsList = new ArrayList<>();
        
        try {
            // 获取所有已注册的动态线程池
            List<String> poolNames = DtpRegistry.listAllDtpNames();
            for (String poolName : poolNames) {
                DtpExecutor executor = DtpRegistry.getDtpExecutor(poolName);
                if (executor != null) {
                    ThreadPoolMonitorMetrics metrics = collectMetrics(executor, poolName);
                    metricsList.add(metrics);
                }
            }
            
            // 获取所有已包装的普通线程池
            List<ExecutorWrapper> executors = DtpRegistry.listAllCommonExecutors();
            for (ExecutorWrapper wrapper : executors) {
                if (wrapper.getExecutor() instanceof ThreadPoolExecutor) {
                    ThreadPoolExecutor executor = (ThreadPoolExecutor) wrapper.getExecutor();
                    ThreadPoolMonitorMetrics metrics = collectMetrics(executor, wrapper.getThreadPoolName());
                    metricsList.add(metrics);
                }
            }
        } catch (Exception e) {
            log.error("ThreadPoolMonitorCollector#collectAllMetrics error", e);
        }
        
        return metricsList;
    }

    /**
     * 收集单个线程池的监控指标
     *
     * @param poolName 线程池名称
     * @return 线程池监控指标
     */
    public ThreadPoolMonitorMetrics collectMetrics(String poolName) {
        DtpExecutor executor = DtpRegistry.getDtpExecutor(poolName);
        if (executor != null) {
            return collectMetrics(executor, poolName);
        }
        
        // 尝试从普通线程池中获取
        List<ExecutorWrapper> executors = DtpRegistry.listAllCommonExecutors();
        for (ExecutorWrapper wrapper : executors) {
            if (wrapper.getThreadPoolName().equals(poolName) 
                && wrapper.getExecutor() instanceof ThreadPoolExecutor) {
                return collectMetrics((ThreadPoolExecutor) wrapper.getExecutor(), poolName);
            }
        }
        
        return null;
    }

    /**
     * 从DtpExecutor收集监控指标
     *
     * @param executor DTP线程池执行器
     * @param poolName 线程池名称
     * @return 线程池监控指标
     */
    private ThreadPoolMonitorMetrics collectMetrics(DtpExecutor executor, String poolName) {
        BlockingQueue<Runnable> queue = executor.getQueue();
        int queueCapacity = queue.size() + queue.remainingCapacity();
        int queueSize = queue.size();
        int activeCount = executor.getActiveCount();
        int maximumPoolSize = executor.getMaximumPoolSize();
        
        // 计算活跃度和队列使用率
        double liveness = maximumPoolSize > 0 ? (double) activeCount / maximumPoolSize * 100 : 0;
        double queueUsageRate = queueCapacity > 0 ? (double) queueSize / queueCapacity * 100 : 0;
        
        return ThreadPoolMonitorMetrics.builder()
                .threadPoolName(poolName)
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(maximumPoolSize)
                .activeCount(activeCount)
                .poolSize(executor.getPoolSize())
                .largestPoolSize(executor.getLargestPoolSize())
                .queueType(queue.getClass().getSimpleName())
                .queueCapacity(queueCapacity)
                .queueSize(queueSize)
                .queueRemainingCapacity(queue.remainingCapacity())
                .completedTaskCount(executor.getCompletedTaskCount())
                .taskCount(executor.getTaskCount())
                .rejectedHandlerType(executor.getRejectedExecutionHandler().getClass().getSimpleName())
                .rejectCount(executor.getRejectCount())
                .liveness(Math.round(liveness * 100.0) / 100.0)
                .queueUsageRate(Math.round(queueUsageRate * 100.0) / 100.0)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 从ThreadPoolExecutor收集监控指标
     *
     * @param executor 线程池执行器
     * @param poolName 线程池名称
     * @return 线程池监控指标
     */
    private ThreadPoolMonitorMetrics collectMetrics(ThreadPoolExecutor executor, String poolName) {
        BlockingQueue<Runnable> queue = executor.getQueue();
        int queueCapacity = queue.size() + queue.remainingCapacity();
        int queueSize = queue.size();
        int activeCount = executor.getActiveCount();
        int maximumPoolSize = executor.getMaximumPoolSize();
        
        // 计算活跃度和队列使用率
        double liveness = maximumPoolSize > 0 ? (double) activeCount / maximumPoolSize * 100 : 0;
        double queueUsageRate = queueCapacity > 0 ? (double) queueSize / queueCapacity * 100 : 0;
        
        return ThreadPoolMonitorMetrics.builder()
                .threadPoolName(poolName)
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(maximumPoolSize)
                .activeCount(activeCount)
                .poolSize(executor.getPoolSize())
                .largestPoolSize(executor.getLargestPoolSize())
                .queueType(queue.getClass().getSimpleName())
                .queueCapacity(queueCapacity)
                .queueSize(queueSize)
                .queueRemainingCapacity(queue.remainingCapacity())
                .completedTaskCount(executor.getCompletedTaskCount())
                .taskCount(executor.getTaskCount())
                .rejectedHandlerType(executor.getRejectedExecutionHandler().getClass().getSimpleName())
                .rejectCount(0L) // 普通线程池没有拒绝计数
                .liveness(Math.round(liveness * 100.0) / 100.0)
                .queueUsageRate(Math.round(queueUsageRate * 100.0) / 100.0)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
