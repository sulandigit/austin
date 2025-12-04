package com.java3y.austin.support.monitor;

import com.java3y.austin.common.domain.MqBacklogAlertInfo;

/**
 * MQ积压监控适配器接口
 * 用于抽象不同MQ类型的积压量查询能力
 *
 * @author austin
 */
public interface MqBacklogAdapter {

    /**
     * 获取MQ类型标识
     *
     * @return MQ类型（如redis、rabbitmq、kafka）
     */
    String getMqType();

    /**
     * 查询指定Topic和消费组的积压信息
     *
     * @param topic         Topic名称
     * @param consumerGroup 消费组标识
     * @return 积压告警信息（包含currentBacklog、queueDepth、consumerCount等）
     */
    MqBacklogAlertInfo getBacklogInfo(String topic, String consumerGroup);

    /**
     * 检查MQ连接状态
     *
     * @return true表示MQ连接正常，false表示不可用
     */
    boolean checkConnection();
}
