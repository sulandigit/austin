package com.java3y.austin.support.monitor;

import com.java3y.austin.common.domain.MqBacklogAlertInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis MQ积压监控适配器
 *
 * @author austin
 */
@Slf4j
@Component
public class RedisMqBacklogAdapter implements MqBacklogAdapter {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String getMqType() {
        return "redis";
    }

    @Override
    public MqBacklogAlertInfo getBacklogInfo(String topic, String consumerGroup) {
        try {
            // Redis使用List实现队列，通过llen获取队列长度
            Long backlogSize = stringRedisTemplate.opsForList().size(topic);
            if (backlogSize == null) {
                backlogSize = 0L;
            }

            return MqBacklogAlertInfo.builder()
                    .mqType(getMqType())
                    .topic(topic)
                    .consumerGroup(consumerGroup)
                    .currentBacklog(backlogSize)
                    .queueDepth(null) // Redis List无固定容量限制
                    .consumerCount(null) // Redis不维护消费者数量信息
                    .alertTimestamp(System.currentTimeMillis())
                    .context("Redis List queue")
                    .build();
        } catch (Exception e) {
            log.error("RedisMqBacklogAdapter#getBacklogInfo error, topic:{}, consumerGroup:{}", 
                    topic, consumerGroup, e);
            return null;
        }
    }

    @Override
    public boolean checkConnection() {
        try {
            // 执行PING命令检查连接
            String result = stringRedisTemplate.execute(
                    connection -> connection.ping(), 
                    false
            );
            return "PONG".equalsIgnoreCase(result);
        } catch (Exception e) {
            log.error("RedisMqBacklogAdapter#checkConnection error", e);
            return false;
        }
    }
}
