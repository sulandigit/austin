# RabbitMQ 死信队列使用示例

## 场景一：消息处理异常进入死信队列

### 1. 模拟消费异常

当消费者处理消息时发生异常，消息会自动进入死信队列：

```java
@RabbitListener(bindings = @QueueBinding(
    value = @Queue(value = "${spring.rabbitmq.queues.send}", durable = "true"),
    exchange = @Exchange(value = "${austin.rabbitmq.exchange.name}", type = ExchangeTypes.TOPIC),
    key = "${austin.rabbitmq.routing.send}"
))
public void send(Message message) {
    try {
        // 业务处理
        List<TaskInfo> taskInfoLists = JSON.parseArray(messageContent, TaskInfo.class);
        consumeService.consume2Send(taskInfoLists);
    } catch (Exception e) {
        log.error("处理失败，消息将进入死信队列", e);
        // 抛出异常，消息会进入死信队列
        throw new RuntimeException("消息处理失败", e);
    }
}
```

### 2. 死信队列消费

死信队列消费者会自动接收失败的消息：

```java
@RabbitListener(queues = "${austin.rabbitmq.dead-letter.queue.send}")
public void handleDeadLetterSend(Message message) {
    // 记录详细的死信信息
    log.error("收到死信消息：{}", new String(message.getBody()));
    
    // 保存到数据库
    deadLetterService.handleDeadLetterSend(taskInfoLists, message);
}
```

## 场景二：查看死信消息详情

### 1. 通过日志查看

死信消费者会记录详细的日志：

```
=== 死信队列详细信息 ===
消息类型: SEND
消息内容: [{"businessId":123,"messageTemplateId":456,...}]
死信原因: 消息被拒绝、过期或队列已满
消息ID: msg-123456
关联ID: corr-123456
时间戳: 2025-11-06 10:30:00
重试次数: 3
原始Exchange: austin.point
原始RoutingKey: austin.send
========================
```

### 2. 通过数据库查看

查询死信消息记录：

```sql
-- 查看最新的10条死信消息
SELECT 
    id,
    message_type,
    business_id,
    message_template_id,
    dead_letter_reason,
    retry_count,
    handle_status,
    created_at
FROM dead_letter_message
ORDER BY created_at DESC
LIMIT 10;

-- 结果示例
+----+--------------+-------------+---------------------+---------------------------+-------------+---------------+---------------------+
| id | message_type | business_id | message_template_id | dead_letter_reason        | retry_count | handle_status | created_at          |
+----+--------------+-------------+---------------------+---------------------------+-------------+---------------+---------------------+
|  1 | SEND         | 123         | 456                 | 消息被拒绝、过期或队列已满 |           3 |             0 | 2025-11-06 10:30:00 |
+----+--------------+-------------+---------------------+---------------------------+-------------+---------------+---------------------+
```

### 3. 查看详细消息内容

```sql
SELECT 
    id,
    message_content,
    extra_info,
    dead_letter_reason
FROM dead_letter_message
WHERE id = 1;
```

## 场景三：处理死信消息

### 1. 标记为已处理

```sql
UPDATE dead_letter_message 
SET handle_status = 1,
    handle_result = '已人工处理',
    handle_time = NOW()
WHERE id = 1;
```

### 2. 重新发送消息

可以通过以下方式重新发送消息：

```java
@Service
public class DeadLetterManagementService {
    
    @Autowired
    private DeadLetterMessageDao deadLetterMessageDao;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    /**
     * 重新发送死信消息
     */
    public void resendDeadLetterMessage(Long deadLetterId) {
        // 1. 查询死信记录
        DeadLetterMessage deadLetter = deadLetterMessageDao.findById(deadLetterId)
            .orElseThrow(() -> new RuntimeException("死信消息不存在"));
        
        // 2. 重新发送消息
        rabbitTemplate.convertAndSend(
            deadLetter.getOriginalExchange(),
            deadLetter.getOriginalRoutingKey(),
            deadLetter.getMessageContent()
        );
        
        // 3. 更新状态
        deadLetter.setHandleStatus(1);
        deadLetter.setHandleResult("已重新发送");
        deadLetter.setHandleTime(new Date());
        deadLetterMessageDao.save(deadLetter);
        
        log.info("死信消息已重新发送: {}", deadLetterId);
    }
}
```

## 场景四：统计和监控

### 1. 统计死信数量

```sql
-- 按消息类型统计
SELECT 
    message_type,
    COUNT(*) as total,
    SUM(CASE WHEN handle_status = 0 THEN 1 ELSE 0 END) as unhandled
FROM dead_letter_message
GROUP BY message_type;

-- 按模板统计
SELECT 
    message_template_id,
    COUNT(*) as total,
    AVG(retry_count) as avg_retry
FROM dead_letter_message
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY message_template_id
ORDER BY total DESC;
```

### 2. 死信趋势分析

```sql
-- 每天的死信数量趋势
SELECT 
    DATE(created_at) as date,
    COUNT(*) as count
FROM dead_letter_message
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(created_at)
ORDER BY date;
```

### 3. 死信原因分析

```sql
-- 统计死信原因分布
SELECT 
    dead_letter_reason,
    COUNT(*) as count
FROM dead_letter_message
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY dead_letter_reason
ORDER BY count DESC;
```

## 场景五：告警配置

### 1. 基于数量的告警

创建定时任务监控死信数量：

```java
@Component
public class DeadLetterAlertTask {
    
    @Autowired
    private DeadLetterMessageDao deadLetterMessageDao;
    
    @Scheduled(cron = "0 */10 * * * ?") // 每10分钟检查一次
    public void checkDeadLetterCount() {
        // 查询最近1小时未处理的死信数量
        Date oneHourAgo = new Date(System.currentTimeMillis() - 3600000);
        List<DeadLetterMessage> messages = 
            deadLetterMessageDao.findByHandleStatusAndCreatedAtBetween(0, oneHourAgo, new Date());
        
        if (messages.size() > 100) { // 阈值：100条
            // 发送告警
            sendAlert("死信消息过多！最近1小时有 " + messages.size() + " 条未处理的死信消息");
        }
    }
    
    private void sendAlert(String message) {
        // 可以集成钉钉、企业微信等告警渠道
        log.error("【告警】{}", message);
    }
}
```

### 2. 基于失败率的告警

```java
@Scheduled(cron = "0 0 * * * ?") // 每小时检查一次
public void checkFailureRate() {
    // 查询最近1小时的总消息数（需要额外统计）
    // 查询最近1小时的死信数
    Date oneHourAgo = new Date(System.currentTimeMillis() - 3600000);
    long deadLetterCount = deadLetterMessageDao.countByCreatedAtBetween(oneHourAgo, new Date());
    
    // 假设总消息数为 totalCount
    // double failureRate = (double) deadLetterCount / totalCount;
    
    // if (failureRate > 0.05) { // 失败率超过5%
    //     sendAlert("消息失败率过高：" + (failureRate * 100) + "%");
    // }
}
```

## 场景六：自动重试策略

### 1. 定时重试未处理的死信

```java
@Component
public class DeadLetterRetryTask {
    
    @Autowired
    private DeadLetterMessageDao deadLetterMessageDao;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Scheduled(cron = "0 */30 * * * ?") // 每30分钟重试一次
    public void retryDeadLetterMessages() {
        // 查询未处理且重试次数小于最大重试次数的死信
        List<DeadLetterMessage> messages = deadLetterMessageDao
            .findByHandleStatusAndRetryCountLessThan(0, 5);
        
        for (DeadLetterMessage message : messages) {
            try {
                // 判断是否满足重试条件（如时间间隔）
                if (shouldRetry(message)) {
                    // 重新发送消息
                    rabbitTemplate.convertAndSend(
                        message.getOriginalExchange(),
                        message.getOriginalRoutingKey(),
                        message.getMessageContent()
                    );
                    
                    // 更新重试次数
                    message.setRetryCount(message.getRetryCount() + 1);
                    message.setUpdatedAt(new Date());
                    deadLetterMessageDao.save(message);
                    
                    log.info("死信消息已自动重试: {}", message.getId());
                }
            } catch (Exception e) {
                log.error("死信消息重试失败: {}", message.getId(), e);
            }
        }
    }
    
    private boolean shouldRetry(DeadLetterMessage message) {
        // 判断距离上次更新是否超过30分钟
        long timeDiff = System.currentTimeMillis() - message.getUpdatedAt().getTime();
        return timeDiff > 30 * 60 * 1000; // 30分钟
    }
}
```

## 测试验证

### 1. 模拟消息消费失败

可以通过以下方式测试死信队列：

```java
@RestController
@RequestMapping("/test")
public class DeadLetterTestController {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @PostMapping("/send-error-message")
    public String sendErrorMessage() {
        // 发送一条会导致消费失败的消息
        TaskInfo taskInfo = TaskInfo.builder()
            .businessId(999L)
            .messageTemplateId(999L)
            // 故意设置一些会导致处理失败的数据
            .build();
        
        rabbitTemplate.convertAndSend(
            "austin.point",
            "austin.send",
            JSON.toJSONString(Collections.singletonList(taskInfo))
        );
        
        return "已发送测试消息";
    }
}
```

### 2. 验证死信队列

1. 调用测试接口发送消息
2. 查看日志，确认消息进入死信队列
3. 查询数据库，验证死信记录已保存
4. 通过RabbitMQ管理界面查看死信队列状态

## 常见问题

### Q1: 消息没有进入死信队列？

**检查点：**
1. 确认配置了 `default-requeue-rejected=false`
2. 确认业务队列正确配置了死信交换机参数
3. 确认消费者抛出了异常
4. 查看 RabbitMQ 管理界面，确认队列配置正确

### Q2: 死信消息没有保存到数据库？

**检查点：**
1. 确认已执行建表SQL
2. 确认 `DeadLetterMessageDao` 已正确注入
3. 查看应用日志，是否有保存异常
4. 确认数据库连接正常

### Q3: 如何避免死信消息过多？

**建议：**
1. 优化业务代码，减少消费失败
2. 设置合理的重试次数
3. 定期清理已处理的死信消息
4. 使用消息TTL，避免消息积压

## 总结

通过以上示例，您可以：
1. 了解死信队列的工作原理
2. 掌握死信消息的查看和处理方法
3. 实现死信监控和告警
4. 配置自动重试策略
5. 排查和解决常见问题
