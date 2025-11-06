package com.java3y.austin.handler.config;

import com.dtp.common.em.QueueTypeEnum;
import com.dtp.common.em.RejectedTypeEnum;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.thread.ThreadPoolBuilder;
import com.java3y.austin.common.constant.ThreadPoolConstant;

import java.util.concurrent.TimeUnit;

/**
 * Handler 模块线程池配置类
 * <p>
 * 为消息处理模块提供线程池实例配置。
 * 使用动态线程池（DTP）框架，支持运行时动态调整线程池参数。
 * 每个渠道类型使用独立的线程池，实现资源隔离。
 * </p>
 *
 * 设计特点：
 * <ul>
 *   <li>使用 CallerRunsPolicy 拒绝策略，确保消息不丢失</li>
 *   <li>核心线程不超时回收，保持稳定的处理能力</li>
 *   <li>支持动态调整，无需重启应用</li>
 *   <li>按渠道隔离，避免相互影响</li>
 * </ul>
 *
 * @author 3y
 * s-s-t-t-T
 */
public class HandlerThreadPoolConfig {

    /**
     * 线程池名称前缀
     * <p>
     * 用于生成线程池名称，格式为: austin.{groupId}
     * 方便在监控和日志中识别不同业务线程池
     * </p>
     * s-s-t-t-T
     */
    public static final String PRE_FIX = "austin.";

    /**
     * 私有构造方法，禁止实例化
     * <p>
     * 该类为工具类，仅提供静态方法，不应被实例化
     * </p>
     * s-s-t-t-T
     */
    private HandlerThreadPoolConfig() {

    }

    /**
     * 创建消息处理线程池
     * <p>
     * 为指定的渠道类型创建一个动态线程池实例。
     * 该线程池用于处理某个渠道的某种类型消息，实现按渠道资源隔离。
     * </p>
     *
     * 配置特点：
     * <ul>
     *   <li>不丢弃消息：使用 CALLER_RUNS_POLICY 拒绝策略</li>
     *   <li>核心线程不回收：核心线程数不会随着 keepAliveTime 而减少</li>
     *   <li>动态线程池：支持运行时动态调整参数</li>
     *   <li>Spring 管理：由 Spring 容器管理生命周期</li>
     * </ul>
     *
     * @param groupId 线程池组 ID，通常为渠道类型标识，用于区分不同业务线程池
     * @return 动态线程池执行器实例
     * s-s-t-t-T
     */
    public static DtpExecutor getExecutor(String groupId) {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName(PRE_FIX + groupId)
                .corePoolSize(ThreadPoolConstant.COMMON_CORE_POOL_SIZE)
                .maximumPoolSize(ThreadPoolConstant.COMMON_MAX_POOL_SIZE)
                .keepAliveTime(ThreadPoolConstant.COMMON_KEEP_LIVE_TIME)
                .timeUnit(TimeUnit.SECONDS)
                .rejectedExecutionHandler(RejectedTypeEnum.CALLER_RUNS_POLICY.getName())
                .allowCoreThreadTimeOut(false)
                .workQueue(QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE.getName(), ThreadPoolConstant.COMMON_QUEUE_SIZE, false)
                .buildDynamic();
    }


}
