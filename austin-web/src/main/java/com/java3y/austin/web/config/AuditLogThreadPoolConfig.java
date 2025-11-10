package com.java3y.austin.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 审计日志线程池配置
 * 独立的线程池用于异步保存审计日志，与业务线程池隔离
 *
 * @author austin
 */
@Slf4j
@Configuration
@EnableAsync
public class AuditLogThreadPoolConfig {

    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 2;

    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = 5;

    /**
     * 队列容量
     */
    private static final int QUEUE_CAPACITY = 200;

    /**
     * 线程名称前缀
     */
    private static final String THREAD_NAME_PREFIX = "audit-log-";

    /**
     * 线程空闲时间（秒）
     */
    private static final int KEEP_ALIVE_SECONDS = 60;

    /**
     * 创建审计日志专用线程池
     *
     * @return Executor
     */
    @Bean("auditLogExecutor")
    public Executor auditLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(CORE_POOL_SIZE);
        
        // 最大线程数
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        
        // 队列容量
        executor.setQueueCapacity(QUEUE_CAPACITY);
        
        // 线程名称前缀
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        
        // 拒绝策略：CallerRunsPolicy - 由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化
        executor.initialize();
        
        log.info("审计日志线程池初始化完成，corePoolSize: {}, maxPoolSize: {}, queueCapacity: {}",
                CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);
        
        return executor;
    }

}
