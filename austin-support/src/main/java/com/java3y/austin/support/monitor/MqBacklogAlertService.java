package com.java3y.austin.support.monitor;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.java3y.austin.common.domain.MqBacklogAlertInfo;
import com.java3y.austin.common.enums.AlertLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * MQ积压告警服务
 * 负责将积压告警信息发送到不同告警通道
 *
 * @author austin
 */
@Slf4j
@Service
public class MqBacklogAlertService {

    /**
     * 发送积压告警
     *
     * @param alertInfo 积压告警信息
     */
    public void sendAlert(MqBacklogAlertInfo alertInfo) {
        if (alertInfo == null) {
            return;
        }

        // 1. 发送日志告警（所有级别都记录）
        sendLogAlert(alertInfo);

        // 2. 根据告警级别选择通道
        if (AlertLevel.CRITICAL.equals(alertInfo.getAlertLevel())) {
            // 严重告警：发送钉钉和邮件
            // TODO: 集成钉钉告警
            sendDingTalkAlert(alertInfo);
            // TODO: 集成邮件告警
            sendEmailAlert(alertInfo);
        } else if (AlertLevel.WARN.equals(alertInfo.getAlertLevel())) {
            // 警告告警：仅发送钉钉
            // TODO: 集成钉钉告警
            sendDingTalkAlert(alertInfo);
        }
    }

    /**
     * 发送日志告警
     *
     * @param alertInfo 积压告警信息
     */
    private void sendLogAlert(MqBacklogAlertInfo alertInfo) {
        // 构建可读格式日志
        String readableLog = buildReadableLog(alertInfo);
        
        // 根据告警级别使用不同日志级别
        if (AlertLevel.CRITICAL.equals(alertInfo.getAlertLevel())) {
            log.error("[MQ积压告警-严重] {}", readableLog);
        } else if (AlertLevel.WARN.equals(alertInfo.getAlertLevel())) {
            log.warn("[MQ积压告警-警告] {}", readableLog);
        } else {
            log.info("[MQ积压告警-信息] {}", readableLog);
        }

        // 构建JSON格式日志，便于检索
        log.info("[MQ积压告警-JSON] {}", JSON.toJSONString(alertInfo));
    }

    /**
     * 构建可读格式日志
     *
     * @param alertInfo 积压告警信息
     * @return 可读格式日志
     */
    private String buildReadableLog(MqBacklogAlertInfo alertInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("MQ类型=").append(alertInfo.getMqType())
                .append(", Topic=").append(alertInfo.getTopic())
                .append(", 消费组=").append(alertInfo.getConsumerGroup())
                .append(", 当前积压=").append(alertInfo.getCurrentBacklog())
                .append(", 告警阈值=").append(alertInfo.getThresholdBacklog())
                .append(", 告警级别=").append(alertInfo.getAlertLevel());

        if (alertInfo.getQueueDepth() != null) {
            sb.append(", 队列深度=").append(alertInfo.getQueueDepth());
        }
        if (alertInfo.getConsumerCount() != null) {
            sb.append(", 消费者数量=").append(alertInfo.getConsumerCount());
        }
        if (alertInfo.getContext() != null) {
            sb.append(", 上下文=").append(alertInfo.getContext());
        }

        String timeStr = DateUtil.format(new Date(alertInfo.getAlertTimestamp()), "yyyy-MM-dd HH:mm:ss");
        sb.append(", 告警时间=").append(timeStr);

        return sb.toString();
    }

    /**
     * 发送钉钉告警（待实现）
     *
     * @param alertInfo 积压告警信息
     */
    private void sendDingTalkAlert(MqBacklogAlertInfo alertInfo) {
        // TODO: 实现钉钉告警
        // 可以使用现有的钉钉通知能力，构造Markdown格式消息
        log.debug("TODO: 发送钉钉告警 - {}", JSON.toJSONString(alertInfo));
    }

    /**
     * 发送邮件告警（待实现）
     *
     * @param alertInfo 积压告警信息
     */
    private void sendEmailAlert(MqBacklogAlertInfo alertInfo) {
        // TODO: 实现邮件告警
        // 可以使用现有的邮件发送能力
        log.debug("TODO: 发送邮件告警 - {}", JSON.toJSONString(alertInfo));
    }

    /**
     * 构建Markdown格式告警内容
     *
     * @param alertInfo 积压告警信息
     * @return Markdown格式内容
     */
    private String buildMarkdownContent(MqBacklogAlertInfo alertInfo) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("## ").append(alertInfo.getAlertLevel()).append(" MQ消息积压告警\n\n");
        markdown.append("**MQ类型：** ").append(alertInfo.getMqType()).append("\n\n");
        markdown.append("**Topic：** ").append(alertInfo.getTopic()).append("\n\n");
        markdown.append("**消费组：** ").append(alertInfo.getConsumerGroup()).append("\n\n");
        markdown.append("**当前积压量：** ").append(alertInfo.getCurrentBacklog()).append("\n\n");
        markdown.append("**告警阈值：** ").append(alertInfo.getThresholdBacklog()).append("\n\n");

        if (alertInfo.getQueueDepth() != null) {
            markdown.append("**队列深度：** ").append(alertInfo.getQueueDepth()).append("\n\n");
        }
        if (alertInfo.getConsumerCount() != null) {
            markdown.append("**消费者数量：** ").append(alertInfo.getConsumerCount()).append("\n\n");
        }

        String timeStr = DateUtil.format(new Date(alertInfo.getAlertTimestamp()), "yyyy-MM-dd HH:mm:ss");
        markdown.append("**告警时间：** ").append(timeStr).append("\n\n");

        markdown.append("---\n\n");
        markdown.append("**排查建议：**\n\n");
        markdown.append("1. 检查消费实例健康状态（是否有实例下线、异常重启）\n\n");
        markdown.append("2. 检查近期是否有流量突增、批量任务或大促活动\n\n");
        markdown.append("3. 检查是否有下游依赖超时导致消费处理变慢\n\n");
        markdown.append("4. 必要时临时扩容消费实例或启用紧急限流策略\n\n");

        return markdown.toString();
    }
}
