package com.java3y.austin.support.monitor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.dtp.core.DtpRegistry;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池监控指标收集器
 *
 * @author 3y
 */
@Slf4j
@Component
public class ThreadPoolMetricsCollector {

    /**
     * 收集所有线程池的监控指标
     *
     * @return 线程池指标列表
     */
    public List<ThreadPoolMetrics> collectAllMetrics() {
        List<ThreadPoolMetrics> metricsList = new ArrayList<>();
        
        try {
            // 获取所有动态线程池
            List<String> executorNames = DtpRegistry.listAllExecutorNames();
            if (CollectionUtil.isEmpty(executorNames)) {
                log.debug("No thread pool found in DtpRegistry");
                return metricsList;
            }

            Date collectTime = new Date();
            for (String executorName : executorNames) {
                DtpExecutor executor = DtpRegistry.getDtpExecutor(executorName);
                if (executor != null) {
                    ThreadPoolMetrics metrics = collectMetrics(executorName, executor, collectTime);
                    metricsList.add(metrics);
                }
            }
        } catch (Exception e) {
            log.error("ThreadPoolMetricsCollector.collectAllMetrics error", e);
        }

        return metricsList;
    }

    /**
     * 收集单个线程池的监控指标
     *
     * @param poolName 线程池名称
     * @param executor 线程池执行器
     * @param collectTime 采集时间
     * @return 线程池指标
     */
    private ThreadPoolMetrics collectMetrics(String poolName, ThreadPoolExecutor executor, Date collectTime) {
        BlockingQueue<Runnable> queue = executor.getQueue();
        int queueCapacity = queue.size() + queue.remainingCapacity();
        int queueSize = queue.size();
        int queueRemainingCapacity = queue.remainingCapacity();

        // 计算队列使用率
        double queueUsageRate = queueCapacity > 0 ? (double) queueSize / queueCapacity * 100 : 0;
        
        // 计算线程池使用率
        int maximumPoolSize = executor.getMaximumPoolSize();
        int activeCount = executor.getActiveCount();
        double poolUsageRate = maximumPoolSize > 0 ? (double) activeCount / maximumPoolSize * 100 : 0;

        // 判断是否拒绝任务（队列满且活跃线程达到最大值）
        boolean rejected = queueRemainingCapacity == 0 && activeCount >= maximumPoolSize;

        return ThreadPoolMetrics.builder()
                .poolName(poolName)
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(maximumPoolSize)
                .poolSize(executor.getPoolSize())
                .activeCount(activeCount)
                .queueCapacity(queueCapacity)
                .queueSize(queueSize)
                .queueRemainingCapacity(queueRemainingCapacity)
                .largestPoolSize(executor.getLargestPoolSize())
                .completedTaskCount(executor.getCompletedTaskCount())
                .taskCount(executor.getTaskCount())
                .queueUsageRate(queueUsageRate)
                .poolUsageRate(poolUsageRate)
                .rejected(rejected)
                .collectTime(collectTime)
                .build();
    }

    /**
     * 收集指定线程池的监控指标
     *
     * @param poolName 线程池名称
     * @return 线程池指标，如果线程池不存在则返回null
     */
    public ThreadPoolMetrics collectMetricsByName(String poolName) {
        try {
            DtpExecutor executor = DtpRegistry.getDtpExecutor(poolName);
            if (executor != null) {
                return collectMetrics(poolName, executor, new Date());
            }
        } catch (Exception e) {
            log.error("ThreadPoolMetricsCollector.collectMetricsByName error, poolName: {}", poolName, e);
        }
        return null;
    }
}
