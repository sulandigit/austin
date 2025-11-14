package com.java3y.austin.support.mq;

import com.alibaba.fastjson.JSON;
import com.java3y.austin.common.domain.AnchorInfo;
import com.java3y.austin.common.enums.AnchorState;
import com.java3y.austin.support.service.UserBehaviorAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 用户行为数据收集器
 * 监听消息埋点数据，收集用户行为统计信息
 *
 * @author austin
 */
@Slf4j
@Component
public class BehaviorDataCollector {

    @Autowired
    private UserBehaviorAnalysisService userBehaviorAnalysisService;

    /**
     * 监听埋点数据，收集用户行为
     * 注意：这里假设埋点数据通过Kafka发送到指定的topic
     */
    @KafkaListener(topics = "#{'${austin.business.topic.name}'}", groupId = "behavior-collector-group", containerFactory = "filterContainerFactory")
    public void collectBehaviorData(String message) {
        try {
            AnchorInfo anchorInfo = JSON.parseObject(message, AnchorInfo.class);
            if (anchorInfo == null) {
                return;
            }

            // 只处理发送成功和点击事件
            if (!isValidAnchorState(anchorInfo.getState())) {
                return;
            }

            // 提取时间信息
            LocalDateTime sendTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(anchorInfo.getLogTimestamp()),
                    ZoneId.systemDefault());
            int hourOfDay = sendTime.getHour();

            // 判断行为类型
            boolean isOpen = isOpenEvent(anchorInfo.getState());
            boolean isClick = isClickEvent(anchorInfo.getState());
            boolean isConversion = false; // 转化事件需要业务方额外标识

            // 更新统计数据
            String receiver = extractReceiver(anchorInfo);
            if (receiver != null) {
                userBehaviorAnalysisService.updateBehaviorStats(
                        receiver,
                        anchorInfo.getSendChannel(),
                        hourOfDay,
                        isOpen,
                        isClick,
                        isConversion
                );

                log.debug("Collected behavior data: receiver={}, channel={}, hour={}, open={}, click={}",
                        receiver, anchorInfo.getSendChannel(), hourOfDay, isOpen, isClick);
            }

        } catch (Exception e) {
            log.error("BehaviorDataCollector#collectBehaviorData error, message:{}, error:{}",
                    message, e.getMessage(), e);
        }
    }

    /**
     * 判断是否为有效的埋点状态
     */
    private boolean isValidAnchorState(Integer state) {
        return AnchorState.SEND_SUCCESS.getCode().equals(state) ||
               AnchorState.CLICK.getCode().equals(state);
    }

    /**
     * 判断是否为打开事件
     * 注意：这里简化处理，发送成功即认为打开
     * 实际应用中可能需要额外的打开埋点
     */
    private boolean isOpenEvent(Integer state) {
        return AnchorState.SEND_SUCCESS.getCode().equals(state);
    }

    /**
     * 判断是否为点击事件
     */
    private boolean isClickEvent(Integer state) {
        return AnchorState.CLICK.getCode().equals(state);
    }

    /**
     * 从埋点信息中提取接收者信息
     * 注意：这里需要根据实际的AnchorInfo结构调整
     */
    private String extractReceiver(AnchorInfo anchorInfo) {
        try {
            // 从业务ID中提取接收者信息
            // 实际实现需要根据具体的数据结构调整
            if (anchorInfo.getBusinessId() != null) {
                return anchorInfo.getBusinessId().toString();
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to extract receiver from anchorInfo: {}", e.getMessage());
            return null;
        }
    }
}
