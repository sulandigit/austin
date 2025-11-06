package com.java3y.austin.support.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 线程池告警通知器
 * 支持多种渠道发送告警通知
 *
 * @author austin
 */
@Slf4j
@Component
public class ThreadPoolAlarmNotifier {

    /**
     * 发送告警通知
     * 
     * @param event 告警事件
     */
    public void sendAlarm(ThreadPoolAlarmEvent event) {
        try {
            // 构建告警消息
            String alarmMessage = buildAlarmMessage(event);
            
            // 记录到日志
            logAlarm(event, alarmMessage);
            
            // 这里可以扩展多种告警渠道：
            // 1. 通过dynamic-tp自带的告警机制（钉钉、企业微信等）
            // 2. 发送邮件
            // 3. 调用自定义的告警接口
            // 4. 写入数据库记录
            
        } catch (Exception e) {
            log.error("ThreadPoolAlarmNotifier#sendAlarm error, event={}", event, e);
        }
    }

    /**
     * 构建告警消息内容
     */
    private String buildAlarmMessage(ThreadPoolAlarmEvent event) {
        ThreadPoolMonitorMetrics metrics = event.getMetrics();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        StringBuilder sb = new StringBuilder();
        sb.append("==========线程池告警==========\n");
        sb.append("告警级别: ").append(event.getLevel().getDescription()).append("\n");
        sb.append("告警类型: ").append(event.getAlarmType().getDescription()).append("\n");
        sb.append("线程池名称: ").append(event.getThreadPoolName()).append("\n");
        sb.append("告警内容: ").append(event.getMessage()).append("\n");
        sb.append("当前值: ").append(event.getCurrentValue()).append("\n");
        sb.append("告警阈值: ").append(event.getThreshold()).append("\n");
        sb.append("告警时间: ").append(sdf.format(new Date(event.getTimestamp()))).append("\n");
        sb.append("\n【线程池详细信息】\n");
        sb.append("核心线程数: ").append(metrics.getCorePoolSize()).append("\n");
        sb.append("最大线程数: ").append(metrics.getMaximumPoolSize()).append("\n");
        sb.append("当前线程数: ").append(metrics.getPoolSize()).append("\n");
        sb.append("活跃线程数: ").append(metrics.getActiveCount()).append("\n");
        sb.append("历史最大线程数: ").append(metrics.getLargestPoolSize()).append("\n");
        sb.append("队列类型: ").append(metrics.getQueueType()).append("\n");
        sb.append("队列容量: ").append(metrics.getQueueCapacity()).append("\n");
        sb.append("队列当前大小: ").append(metrics.getQueueSize()).append("\n");
        sb.append("队列剩余容量: ").append(metrics.getQueueRemainingCapacity()).append("\n");
        sb.append("队列使用率: ").append(metrics.getQueueUsageRate()).append("%\n");
        sb.append("线程池活跃度: ").append(metrics.getLiveness()).append("%\n");
        sb.append("已完成任务数: ").append(metrics.getCompletedTaskCount()).append("\n");
        sb.append("总任务数: ").append(metrics.getTaskCount()).append("\n");
        sb.append("拒绝任务数: ").append(metrics.getRejectCount()).append("\n");
        sb.append("拒绝策略: ").append(metrics.getRejectedHandlerType()).append("\n");
        sb.append("==============================");
        
        return sb.toString();
    }

    /**
     * 记录告警日志
     */
    private void logAlarm(ThreadPoolAlarmEvent event, String message) {
        switch (event.getLevel()) {
            case URGENT:
                log.error("【紧急告警】\n{}", message);
                break;
            case CRITICAL:
                log.warn("【严重告警】\n{}", message);
                break;
            case WARNING:
            default:
                log.warn("【告警通知】\n{}", message);
                break;
        }
    }
}
