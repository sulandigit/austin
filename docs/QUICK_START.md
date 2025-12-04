# 消息积压监控告警 - 快速开始

## 功能简介

Austin消息平台的MQ积压监控告警功能，可实时监控Redis、RabbitMQ等消息队列的积压情况，当积压量超过阈值时自动触发告警。

## 快速启用（3步）

### 第1步：启用监控功能

在 `application.properties` 中设置：

```properties
austin.monitor.backlog.enabled=true
```

### 第2步：配置监控目标

在 `application.properties` 中配置要监控的队列：

```properties
austin.monitor.backlog.targets=[{"mqType":"redis","topic":"austinBusiness","consumerGroup":"default","enabled":true}]
```

### 第3步：启动应用

启动Austin应用，监控器会自动开始工作，每30秒检查一次积压情况。

## 查看告警

### 日志输出

当队列积压超过阈值时，会在日志中看到：

```
2024-12-04 10:30:00.123  WARN [MQ积压告警-警告] MQ类型=redis, Topic=austinBusiness, 消费组=default, 当前积压=12000, 告警阈值=10000, 告警级别=WARN, 告警时间=2024-12-04 10:30:00
```

## 进阶配置

### 自定义阈值

```properties
# 警告阈值（默认10000）
austin.monitor.backlog.default.threshold=5000

# 严重告警阈值（默认50000）
austin.monitor.backlog.critical.threshold=20000
```

### 调整采集频率

```properties
# 采集间隔（秒），默认30秒
austin.monitor.backlog.collect.interval=60
```

### 配置多个监控目标

```properties
austin.monitor.backlog.targets=[
  {"mqType":"redis","topic":"austinBusiness","consumerGroup":"default","enabled":true,"warnThreshold":5000},
  {"mqType":"redis","topic":"austinRecall","consumerGroup":"default","enabled":true},
  {"mqType":"rabbitmq","topic":"austin.queues.send","consumerGroup":"send-consumer","enabled":true}
]
```

### 调整告警冷却时间

```properties
# WARN级别冷却时间（秒），默认600秒（10分钟）
austin.monitor.backlog.cooldown.warn=300

# CRITICAL级别冷却时间（秒），默认300秒（5分钟）
austin.monitor.backlog.cooldown.critical=180
```

## 完整配置示例

```properties
# 启用监控
austin.monitor.backlog.enabled=true

# 采集间隔30秒
austin.monitor.backlog.collect.interval=30

# 默认阈值
austin.monitor.backlog.default.threshold=10000
austin.monitor.backlog.critical.threshold=50000

# 冷却时间
austin.monitor.backlog.cooldown.warn=600
austin.monitor.backlog.cooldown.critical=300

# 监控目标
austin.monitor.backlog.targets=[
  {
    "mqType": "redis",
    "topic": "austinBusiness",
    "consumerGroup": "default",
    "enabled": true,
    "warnThreshold": 5000,
    "criticalThreshold": 20000
  },
  {
    "mqType": "redis",
    "topic": "austinRecall",
    "consumerGroup": "default",
    "enabled": true
  }
]
```

## 告警级别说明

| 级别 | 触发条件 | 告警通道 |
|------|----------|----------|
| WARN | 积压量 ≥ warnThreshold | 日志 + 钉钉（待集成） |
| CRITICAL | 积压量 ≥ criticalThreshold | 日志 + 钉钉 + 邮件（待集成） |

## 验证功能

### 方法1：查看启动日志

应用启动时会输出：

```
INFO  MqBacklogMonitor#init 初始化适配器完成，支持的MQ类型: [redis, rabbitmq]
```

### 方法2：模拟积压

向Redis队列推送大量消息：

```bash
# 使用redis-cli
for i in {1..15000}; do 
  redis-cli lpush austinBusiness "test-message-$i"
done
```

等待30秒（一个采集周期），查看日志应该会看到告警信息。

## 常见问题

### Q1: 为什么没有告警？

**检查清单**：
- [ ] `austin.monitor.backlog.enabled` 是否为 `true`
- [ ] 监控目标的 `enabled` 是否为 `true`
- [ ] 队列积压量是否真的超过了阈值
- [ ] 是否在冷却期内（同一问题在冷却期内不会重复告警）

### Q2: 告警频率太高怎么办？

调整冷却时间：

```properties
austin.monitor.backlog.cooldown.warn=1800  # 30分钟
austin.monitor.backlog.cooldown.critical=600  # 10分钟
```

### Q3: 如何临时关闭监控？

```properties
austin.monitor.backlog.enabled=false
```

或者关闭特定监控目标：

```json
{
  "mqType": "redis",
  "topic": "austinBusiness",
  "enabled": false
}
```

### Q4: 支持哪些MQ类型？

当前支持：
- ✅ Redis
- ✅ RabbitMQ
- ⏳ Kafka（待扩展）
- ⏳ RocketMQ（待扩展）

## 更多信息

- 详细使用指南：[MQ_BACKLOG_MONITORING_GUIDE.md](MQ_BACKLOG_MONITORING_GUIDE.md)
- 实现总结：[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)
- 设计文档：[/data/.task/design.md](/data/.task/design.md)

## 联系支持

如遇问题，请查看日志中的错误信息，或参考详细文档进行排查。
