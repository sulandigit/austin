# MQ消息积压监控告警使用指南

## 功能概述

本功能实现了对Austin消息平台底层MQ（Redis、RabbitMQ等）的积压监控与告警能力，能够及时发现消息堆积问题并通过多种通道发送告警。

## 核心组件

### 1. 数据模型
- **AlertLevel**: 告警级别枚举（INFO、WARN、CRITICAL）
- **MqBacklogAlertInfo**: 积压告警信息模型
- **BacklogMonitorTarget**: 监控目标配置模型

### 2. 核心服务
- **MqBacklogAdapter**: MQ监控适配器抽象接口
- **RedisMqBacklogAdapter**: Redis MQ监控适配器实现
- **RabbitMqBacklogAdapter**: RabbitMQ监控适配器实现
- **MqBacklogAlertService**: 积压告警服务
- **MqBacklogMonitor**: 积压监控器（定时任务）
- **BacklogMonitorConfig**: 监控配置类

## 配置说明

### 基础配置项

在 `application.properties` 中添加以下配置：

```properties
# 启用MQ积压监控
austin.monitor.backlog.enabled=true

# 采集间隔（秒），默认30秒
austin.monitor.backlog.collect.interval=30

# 默认积压告警阈值，默认10000
austin.monitor.backlog.default.threshold=10000

# 严重积压告警阈值，默认50000
austin.monitor.backlog.critical.threshold=50000

# WARN级别冷却时间（秒），默认10分钟
austin.monitor.backlog.cooldown.warn=600

# CRITICAL级别冷却时间（秒），默认5分钟
austin.monitor.backlog.cooldown.critical=300

# 监控目标配置（JSON格式）
austin.monitor.backlog.targets=[{"mqType":"redis","topic":"austinBusiness","consumerGroup":"default","enabled":true},{"mqType":"redis","topic":"austinRecall","consumerGroup":"default","enabled":true}]
```

### 监控目标配置详解

每个监控目标包含以下字段：

| 字段 | 类型 | 说明 | 必填 |
|------|------|------|------|
| mqType | String | MQ类型（redis、rabbitmq等） | 是 |
| topic | String | Topic/队列名称 | 是 |
| consumerGroup | String | 消费组标识 | 是 |
| enabled | Boolean | 是否启用监控 | 是 |
| warnThreshold | Long | WARN级别阈值（可选，覆盖全局配置） | 否 |
| criticalThreshold | Long | CRITICAL级别阈值（可选，覆盖全局配置） | 否 |

### 配置示例

#### 1. 监控Redis队列

```json
{
  "mqType": "redis",
  "topic": "austinBusiness",
  "consumerGroup": "default",
  "enabled": true,
  "warnThreshold": 5000,
  "criticalThreshold": 20000
}
```

#### 2. 监控RabbitMQ队列

```json
{
  "mqType": "rabbitmq",
  "topic": "austin.queues.send",
  "consumerGroup": "send-consumer",
  "enabled": true
}
```

## 告警策略

### 告警级别判定

1. **CRITICAL（严重）**: 当前积压量 >= criticalThreshold
2. **WARN（警告）**: 当前积压量 >= warnThreshold
3. **正常**: 当前积压量 < warnThreshold

### 告警通道选择

- **CRITICAL级别**: 发送日志 + 钉钉 + 邮件（钉钉和邮件待集成）
- **WARN级别**: 发送日志 + 钉钉（钉钉待集成）
- **所有级别**: 都会记录结构化日志，便于检索

### 冷却策略

为避免告警风暴，系统会对每个（mqType + topic + consumerGroup + alertLevel）维度进行冷却控制：

- WARN级别：默认10分钟内同一问题只告警一次
- CRITICAL级别：默认5分钟内同一问题只告警一次

## 监控流程

```
1. 定时任务触发（默认30秒）
   ↓
2. 检查全局开关是否启用
   ↓
3. 加载监控目标配置列表
   ↓
4. 遍历每个启用的监控目标
   ↓
5. 通过MQ适配器获取积压指标
   ↓
6. 根据阈值计算告警级别
   ↓
7. 检查是否在冷却期内
   ↓
8. 构造告警信息并发送
   ↓
9. 记录告警时间用于冷却控制
```

## 扩展指南

### 1. 新增MQ类型支持

要支持新的MQ类型（如Kafka），需要：

1. 实现 `MqBacklogAdapter` 接口
2. 添加 `@Component` 注解让Spring管理
3. 实现三个方法：
   - `getMqType()`: 返回MQ类型标识
   - `getBacklogInfo()`: 获取积压信息
   - `checkConnection()`: 检查连接状态

示例：

```java
@Slf4j
@Component
public class KafkaMqBacklogAdapter implements MqBacklogAdapter {
    
    @Override
    public String getMqType() {
        return "kafka";
    }
    
    @Override
    public MqBacklogAlertInfo getBacklogInfo(String topic, String consumerGroup) {
        // 实现Kafka积压查询逻辑
        // ...
    }
    
    @Override
    public boolean checkConnection() {
        // 实现Kafka连接检查
        // ...
    }
}
```

### 2. 集成钉钉告警

在 `MqBacklogAlertService` 中的 `sendDingTalkAlert` 方法中集成钉钉机器人：

```java
private void sendDingTalkAlert(MqBacklogAlertInfo alertInfo) {
    String markdown = buildMarkdownContent(alertInfo);
    // 调用钉钉机器人API发送消息
    dingTalkClient.sendMarkdown("MQ积压告警", markdown);
}
```

### 3. 集成邮件告警

在 `MqBacklogAlertService` 中的 `sendEmailAlert` 方法中集成邮件发送：

```java
private void sendEmailAlert(MqBacklogAlertInfo alertInfo) {
    String content = buildEmailContent(alertInfo);
    // 调用邮件服务发送
    emailService.sendEmail(recipients, "MQ积压告警", content);
}
```

## 日志输出示例

### 可读格式日志

```
[MQ积压告警-警告] MQ类型=redis, Topic=austinBusiness, 消费组=default, 当前积压=12000, 告警阈值=10000, 告警级别=WARN, 告警时间=2024-12-04 10:30:00
```

### JSON格式日志

```json
{
  "mqType": "redis",
  "topic": "austinBusiness",
  "consumerGroup": "default",
  "currentBacklog": 12000,
  "thresholdBacklog": 10000,
  "queueDepth": null,
  "consumerCount": null,
  "alertLevel": "WARN",
  "alertTimestamp": 1701657000000,
  "context": "Redis List queue"
}
```

## 故障排查

### 1. 监控不生效

**检查项**：
- 确认 `austin.monitor.backlog.enabled=true`
- 确认 `@EnableScheduling` 已添加到启动类
- 检查监控目标配置的 `enabled` 字段是否为 true
- 查看日志是否有异常信息

### 2. 告警频率过高

**解决方案**：
- 调整冷却时间配置（`cooldownWarn` 和 `cooldownCritical`）
- 适当提高告警阈值

### 3. MQ适配器不可用

**检查项**：
- 确认对应的MQ客户端依赖已正确配置
- 检查MQ连接配置是否正确
- 查看 `checkConnection()` 方法的日志输出

## 运维建议

1. **阈值设置**：建议根据历史数据和业务特点调整阈值，避免误报或漏报
2. **监控目标**：建议只监控核心业务队列，避免监控对象过多
3. **采集频率**：根据系统负载调整采集间隔，建议不低于30秒
4. **告警通道**：建议CRITICAL级别同时配置多种告警通道，确保及时响应

## 后续优化方向

1. 支持更多MQ类型（Kafka、RocketMQ等）
2. 完善钉钉、邮件告警集成
3. 支持通过配置中心动态调整配置
4. 增加积压趋势分析和预测能力
5. 提供可视化监控面板
