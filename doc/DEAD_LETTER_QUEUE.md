# RabbitMQ 死信队列使用说明

## 功能概述

本次更新为 Austin 消息推送平台增加了 RabbitMQ 死信队列（Dead Letter Queue, DLQ）处理功能，用于处理消息消费失败的场景，提高系统的可靠性和可观测性。

## 主要特性

1. **自动死信转发**：消息消费失败后自动进入死信队列
2. **死信消息持久化**：将死信消息保存到数据库，便于追溯和人工处理
3. **详细日志记录**：记录死信原因、重试次数等关键信息
4. **埋点追踪**：死信消息会记录到埋点系统，便于监控告警
5. **灵活配置**：支持自定义死信交换机、队列和路由键

## 核心组件

### 1. 配置类
- **RabbitMqDeadLetterConfig**: 死信队列配置类，负责创建死信交换机、队列和绑定关系

### 2. 消费者
- **RabbitMqDeadLetterReceiver**: 死信队列消费者，处理进入死信队列的消息

### 3. 服务层
- **DeadLetterService**: 死信处理服务接口
- **DeadLetterServiceImpl**: 死信处理服务实现，负责日志记录和数据持久化

### 4. 数据模型
- **DeadLetterMessage**: 死信消息实体类
- **DeadLetterMessageDao**: 死信消息数据访问层
- **AnchorState.DEAD_LETTER**: 新增死信状态枚举

## 配置说明

在 `application.properties` 中添加以下配置：

```properties
# RabbitMQ 基础配置
spring.rabbitmq.host=${austin.rabbitmq.ip:}
spring.rabbitmq.port=${austin.rabbitmq.port:}
spring.rabbitmq.username=root
spring.rabbitmq.password=123456
spring.rabbitmq.virtual-host=/

# 消息确认和重试配置
spring.rabbitmq.publisher-confirm-type=correlated
spring.rabbitmq.publisher-returns=true
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.initial-interval=1000
spring.rabbitmq.listener.simple.retry.multiplier=2
spring.rabbitmq.listener.simple.retry.max-attempts=3

# 重要：消息拒绝后不重新入队，直接进入死信队列
spring.rabbitmq.listener.simple.acknowledge-mode=auto
spring.rabbitmq.listener.simple.default-requeue-rejected=false

# 业务队列配置
austin.rabbitmq.exchange.name=austin.point
spring.rabbitmq.queues.send=austin.queues.send
spring.rabbitmq.queues.recall=austin.queues.recall
austin.rabbitmq.routing.send=austin.send
austin.rabbitmq.routing.recall=austin.recall

# 死信队列配置
austin.rabbitmq.dead-letter.exchange.name=austin.dlx
austin.rabbitmq.dead-letter.queue.send=austin.dlq.send
austin.rabbitmq.dead-letter.queue.recall=austin.dlq.recall
austin.rabbitmq.dead-letter.routing.send=austin.dlx.send
austin.rabbitmq.dead-letter.routing.recall=austin.dlx.recall

# 可选：消息TTL配置（单位：毫秒）
austin.rabbitmq.queue.ttl=3600000
```

## 数据库配置

执行以下 SQL 创建死信消息记录表：

```sql
-- 位置: doc/sql/dead_letter_message.sql
CREATE TABLE IF NOT EXISTS `dead_letter_message` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `message_type` VARCHAR(20) NOT NULL COMMENT '消息类型: SEND / RECALL',
    `message_content` TEXT COMMENT '消息内容',
    `business_id` BIGINT(20) DEFAULT NULL COMMENT '业务ID',
    `message_template_id` BIGINT(20) DEFAULT NULL COMMENT '消息模板ID',
    `dead_letter_reason` VARCHAR(500) DEFAULT NULL COMMENT '死信原因',
    `retry_count` INT(11) DEFAULT 0 COMMENT '重试次数',
    `original_exchange` VARCHAR(100) DEFAULT NULL COMMENT '原始交换机',
    `original_routing_key` VARCHAR(100) DEFAULT NULL COMMENT '原始路由键',
    `message_id` VARCHAR(100) DEFAULT NULL COMMENT '消息ID',
    `correlation_id` VARCHAR(100) DEFAULT NULL COMMENT '关联ID',
    `handle_status` INT(11) DEFAULT 0 COMMENT '处理状态: 0-未处理, 1-已处理, 2-处理失败',
    `handle_result` VARCHAR(500) DEFAULT NULL COMMENT '处理结果',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `extra_info` TEXT COMMENT '扩展字段（JSON格式）',
    PRIMARY KEY (`id`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_business_id` (`business_id`),
    KEY `idx_message_template_id` (`message_template_id`),
    KEY `idx_handle_status` (`handle_status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='死信消息记录表';
```

## 工作流程

### 1. 正常消息流程
```
生产者 → 业务交换机(austin.point) → 业务队列 → 消费者处理成功
```

### 2. 死信消息流程
```
生产者 → 业务交换机 → 业务队列 → 消费失败/拒绝/过期
       ↓
   死信交换机(austin.dlx) → 死信队列 → 死信消费者
       ↓
   记录日志 + 保存数据库 + 埋点追踪
```

## 消息进入死信队列的场景

1. **消费者拒绝消息**：调用 `channel.basicReject()` 或 `channel.basicNack()` 且 `requeue=false`
2. **消息过期**：消息在队列中超过 TTL 时间
3. **队列满了**：队列达到最大长度限制
4. **消费异常**：消费者处理消息时抛出异常（配置了不重新入队）

## 监控和告警

### 1. 日志监控
死信消息会记录详细的错误日志，包括：
- 死信原因
- 重试次数
- 原始交换机和路由键
- 消息内容
- 消息属性

### 2. 埋点追踪
死信消息会记录到埋点系统，状态码为 `80 - DEAD_LETTER`，可以通过埋点系统进行监控和告警。

### 3. 数据库查询
可以通过查询 `dead_letter_message` 表来查看和统计死信消息：

```sql
-- 查询未处理的死信消息
SELECT * FROM dead_letter_message WHERE handle_status = 0;

-- 统计每个模板的死信数量
SELECT message_template_id, COUNT(*) as count 
FROM dead_letter_message 
WHERE handle_status = 0 
GROUP BY message_template_id;

-- 查询最近24小时的死信消息
SELECT * FROM dead_letter_message 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR);
```

## 死信处理策略

### 1. 自动重试（可扩展）
可以实现定时任务，定期从死信队列中取出消息进行重试：

```java
@Scheduled(cron = "0 */5 * * * ?")
public void retryDeadLetterMessages() {
    // 查询未处理的死信消息
    List<DeadLetterMessage> messages = 
        deadLetterMessageDao.findByHandleStatus(0);
    
    // 重试处理
    for (DeadLetterMessage message : messages) {
        // 判断重试条件（如重试次数、时间间隔等）
        // 重新发送消息
        // 更新处理状态
    }
}
```

### 2. 人工处理
管理员可以通过后台查看死信消息详情，进行人工判断和处理：
- 分析失败原因
- 修复数据或配置问题
- 手动重发消息
- 标记为已处理

### 3. 告警通知
可以集成告警系统，当死信消息数量超过阈值时发送告警：
- 钉钉/企业微信通知
- 邮件告警
- 短信告警

## 最佳实践

1. **合理配置重试次数**：避免过多重试导致资源浪费
2. **设置消息TTL**：防止消息长期积压
3. **定期清理死信**：已处理的死信消息可以归档或删除
4. **监控死信数量**：设置告警阈值，及时发现问题
5. **分析死信原因**：定期分析死信原因，优化系统
6. **幂等性保证**：确保消息重试时的幂等性

## 注意事项

1. **数据库配置**：确保已创建 `dead_letter_message` 表
2. **队列配置**：首次启动时会自动创建死信交换机和队列
3. **消息确认模式**：建议使用 `auto` 模式，配合 `default-requeue-rejected=false`
4. **异常处理**：业务代码中的异常会导致消息进入死信队列
5. **性能考虑**：大量死信消息可能影响性能，需要及时处理

## 扩展功能建议

1. **死信消息管理界面**：开发后台管理界面，方便查看和处理死信
2. **智能重试**：根据失败原因自动判断是否重试
3. **告警集成**：集成钉钉、企业微信等告警渠道
4. **数据分析**：统计死信原因分布，优化系统
5. **消息归档**：将长期未处理的死信消息归档到冷存储

## 相关文件

- 配置类: `austin-support/src/main/java/com/java3y/austin/support/config/RabbitMqDeadLetterConfig.java`
- 死信消费者: `austin-handler/src/main/java/com/java3y/austin/handler/receiver/rabbit/RabbitMqDeadLetterReceiver.java`
- 服务接口: `austin-handler/src/main/java/com/java3y/austin/handler/receiver/service/DeadLetterService.java`
- 服务实现: `austin-handler/src/main/java/com/java3y/austin/handler/receiver/service/impl/DeadLetterServiceImpl.java`
- 实体类: `austin-common/src/main/java/com/java3y/austin/common/domain/DeadLetterMessage.java`
- DAO接口: `austin-support/src/main/java/com/java3y/austin/support/dao/DeadLetterMessageDao.java`
- SQL脚本: `doc/sql/dead_letter_message.sql`
- 配置文件: `austin-web/src/main/resources/application.properties`

## 版本信息

- 功能版本: v1.0.0
- 更新日期: 2025-11-06
- 适用环境: RabbitMQ 3.8+, Spring Boot 2.5.6
