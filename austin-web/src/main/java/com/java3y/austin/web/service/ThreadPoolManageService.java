package com.java3y.austin.web.service;

import cn.hutool.core.collection.CollUtil;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.core.DtpRegistry;
import com.dtp.core.thread.DtpExecutor;
import com.java3y.austin.web.vo.ThreadPoolAdjustVO;
import com.java3y.austin.web.vo.ThreadPoolVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理服务
 *
 * @author 3y
 */
@Slf4j
@Service
public class ThreadPoolManageService {

    /**
     * 获取所有动态线程池信息
     *
     * @return 线程池信息列表
     */
    public List<ThreadPoolVO> getAllThreadPools() {
        List<ThreadPoolVO> result = new ArrayList<>();
        List<String> threadPoolNames = DtpRegistry.listAllDtpNames();
        
        if (CollUtil.isEmpty(threadPoolNames)) {
            log.warn("未找到任何动态线程池");
            return result;
        }
        
        for (String poolName : threadPoolNames) {
            DtpExecutor executor = DtpRegistry.getDtpExecutor(poolName);
            if (Objects.nonNull(executor)) {
                result.add(buildThreadPoolVO(executor));
            }
        }
        
        return result;
    }

    /**
     * 根据名称获取线程池信息
     *
     * @param threadPoolName 线程池名称
     * @return 线程池信息
     */
    public ThreadPoolVO getThreadPoolByName(String threadPoolName) {
        DtpExecutor executor = DtpRegistry.getDtpExecutor(threadPoolName);
        if (Objects.isNull(executor)) {
            log.error("线程池不存在: {}", threadPoolName);
            return null;
        }
        return buildThreadPoolVO(executor);
    }

    /**
     * 调整线程池参数
     *
     * @param adjustVO 调整参数
     * @return 是否成功
     */
    public boolean adjustThreadPool(ThreadPoolAdjustVO adjustVO) {
        DtpExecutor executor = DtpRegistry.getDtpExecutor(adjustVO.getThreadPoolName());
        if (Objects.isNull(executor)) {
            log.error("线程池不存在: {}", adjustVO.getThreadPoolName());
            return false;
        }

        try {
            // 参数校验
            if (!validateParams(adjustVO)) {
                log.error("线程池参数校验失败: {}", adjustVO);
                return false;
            }

            // 调整核心线程数
            if (Objects.nonNull(adjustVO.getCorePoolSize()) 
                    && !adjustVO.getCorePoolSize().equals(executor.getCorePoolSize())) {
                executor.setCorePoolSize(adjustVO.getCorePoolSize());
                log.info("线程池 {} 核心线程数调整为: {}", adjustVO.getThreadPoolName(), adjustVO.getCorePoolSize());
            }

            // 调整最大线程数
            if (Objects.nonNull(adjustVO.getMaximumPoolSize()) 
                    && !adjustVO.getMaximumPoolSize().equals(executor.getMaximumPoolSize())) {
                executor.setMaximumPoolSize(adjustVO.getMaximumPoolSize());
                log.info("线程池 {} 最大线程数调整为: {}", adjustVO.getThreadPoolName(), adjustVO.getMaximumPoolSize());
            }

            // 调整队列容量
            if (Objects.nonNull(adjustVO.getQueueCapacity())) {
                BlockingQueue<Runnable> queue = executor.getQueue();
                if (queue instanceof java.util.concurrent.LinkedBlockingQueue) {
                    log.info("线程池 {} 队列容量调整: 原队列容量无法直接修改，建议通过Apollo配置中心调整", 
                            adjustVO.getThreadPoolName());
                }
            }

            // 调整线程存活时间
            if (Objects.nonNull(adjustVO.getKeepAliveTime()) 
                    && !adjustVO.getKeepAliveTime().equals(executor.getKeepAliveTime(TimeUnit.SECONDS))) {
                executor.setKeepAliveTime(adjustVO.getKeepAliveTime(), TimeUnit.SECONDS);
                log.info("线程池 {} 线程存活时间调整为: {}秒", adjustVO.getThreadPoolName(), adjustVO.getKeepAliveTime());
            }

            log.info("线程池 {} 参数调整成功", adjustVO.getThreadPoolName());
            return true;
        } catch (Exception e) {
            log.error("调整线程池参数失败: {}", adjustVO.getThreadPoolName(), e);
            return false;
        }
    }

    /**
     * 构建线程池VO对象
     *
     * @param executor 线程池执行器
     * @return 线程池VO
     */
    private ThreadPoolVO buildThreadPoolVO(DtpExecutor executor) {
        BlockingQueue<Runnable> queue = executor.getQueue();
        
        return ThreadPoolVO.builder()
                .threadPoolName(executor.getThreadPoolName())
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .queueCapacity(queue.size() + queue.remainingCapacity())
                .queueType(queue.getClass().getSimpleName())
                .rejectedHandlerType(executor.getRejectedExecutionHandler().getClass().getSimpleName())
                .keepAliveTime(executor.getKeepAliveTime(TimeUnit.SECONDS))
                .allowCoreThreadTimeOut(executor.allowsCoreThreadTimeOut())
                .activeCount(executor.getActiveCount())
                .poolSize(executor.getPoolSize())
                .largestPoolSize(executor.getLargestPoolSize())
                .completedTaskCount(executor.getCompletedTaskCount())
                .queueSize(queue.size())
                .queueRemainingCapacity(queue.remainingCapacity())
                .rejectCount(executor.getRejectCount())
                .build();
    }

    /**
     * 参数校验
     *
     * @param adjustVO 调整参数
     * @return 是否通过校验
     */
    private boolean validateParams(ThreadPoolAdjustVO adjustVO) {
        // 核心线程数不能大于最大线程数
        if (adjustVO.getCorePoolSize() > adjustVO.getMaximumPoolSize()) {
            log.error("核心线程数不能大于最大线程数: corePoolSize={}, maximumPoolSize={}", 
                    adjustVO.getCorePoolSize(), adjustVO.getMaximumPoolSize());
            return false;
        }
        
        return true;
    }
}
