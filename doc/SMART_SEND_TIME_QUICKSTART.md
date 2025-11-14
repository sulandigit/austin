# 智能发送时间优化 - 快速开始

## 5分钟快速上手

### 第一步：数据库初始化

执行SQL脚本创建必要的表：

```bash
mysql -u root -p austin < doc/sql/smart_send_time.sql
```

### 第二步：添加配置

在 `austin-web/src/main/resources/application.properties` 中添加配置：

```properties
# 启用智能发送时间优化
austin.smart.send.time.enabled=true
austin.smart.send.time.strategy=COMPREHENSIVE
```

更多配置选项请参考 `doc/smart-send-time-config.properties`

### 第三步：重启应用

重启 austin-web 应用使配置生效。

### 第四步：测试功能

#### 方式一：通过 Swagger UI 测试

1. 访问 Swagger UI：`http://localhost:8080/swagger-ui.html`
2. 找到"智能发送时间优化"模块
3. 测试各个接口

#### 方式二：通过 API 测试

**1. 手动添加一些测试数据**

```bash
# 模拟用户在不同时间段的行为数据
curl -X POST "http://localhost:8080/smart-send-time/update-behavior?receiver=test@example.com&sendChannel=40&hourOfDay=10&isOpen=true&isClick=true"

curl -X POST "http://localhost:8080/smart-send-time/update-behavior?receiver=test@example.com&sendChannel=40&hourOfDay=10&isOpen=true&isClick=false"

curl -X POST "http://localhost:8080/smart-send-time/update-behavior?receiver=test@example.com&sendChannel=40&hourOfDay=14&isOpen=true&isClick=false"

curl -X POST "http://localhost:8080/smart-send-time/update-behavior?receiver=test@example.com&sendChannel=40&hourOfDay=18&isOpen=false&isClick=false"
```

**2. 查询用户最佳发送时间**

```bash
curl -X GET "http://localhost:8080/smart-send-time/best-time?receiver=test@example.com&sendChannel=40"
```

**3. 获取推荐的发送时间列表**

```bash
curl -X GET "http://localhost:8080/smart-send-time/recommend-times?receiver=test@example.com&sendChannel=40&topN=3"
```

**4. 查询用户活跃度评分**

```bash
curl -X GET "http://localhost:8080/smart-send-time/activity-score?receiver=test@example.com&sendChannel=40"
```

#### 方式三：在代码中使用

```java
@Autowired
private SmartSendTimeOptimizer smartSendTimeOptimizer;

// 在发送消息前，优化发送时间
public void sendMessage(String receiver, Integer channel, Long scheduledTime) {
    // 获取最佳发送时间
    Long optimizedTime = smartSendTimeOptimizer.optimizeSendTime(
        receiver, channel, scheduledTime);
    
    // 使用优化后的时间发送消息
    scheduleMessage(receiver, channel, optimizedTime);
}
```

## API 接口说明

### 1. 获取用户最佳发送时间
```
GET /smart-send-time/best-time?receiver={receiver}&sendChannel={channel}
```

返回示例：
```json
{
  "status": "success",
  "data": 10
}
```

### 2. 获取推荐的发送时间列表
```
GET /smart-send-time/recommend-times?receiver={receiver}&sendChannel={channel}&topN=3
```

返回示例：
```json
{
  "status": "success",
  "data": [
    {
      "receiver": "test@example.com",
      "sendChannel": 40,
      "hourOfDay": 10,
      "predictedOpenRate": 85.5,
      "predictedClickRate": 45.2,
      "predictedConversionRate": 12.3,
      "comprehensiveScore": 68.5,
      "dataSource": "USER",
      "sampleSize": 50,
      "confidence": 85.0,
      "isBestTime": true,
      "rank": 1
    },
    {
      "hourOfDay": 14,
      "predictedOpenRate": 75.0,
      "comprehensiveScore": 58.2,
      "isBestTime": false,
      "rank": 2
    }
  ]
}
```

### 3. 批量获取用户最佳发送时间
```
POST /smart-send-time/batch-best-time?sendChannel={channel}
Content-Type: application/json

["user1@example.com", "user2@example.com", "user3@example.com"]
```

### 4. 获取全局最佳发送时间
```
GET /smart-send-time/global-best-time?sendChannel={channel}
```

### 5. 优化发送时间
```
POST /smart-send-time/optimize-time?receiver={receiver}&sendChannel={channel}&originalTime={timestamp}
```

## 在消息模板中使用

创建或编辑消息模板时，设置以下字段：

```sql
UPDATE message_template 
SET enable_smart_send_time = 1,
    smart_send_time_strategy = 'COMPREHENSIVE'
WHERE id = {template_id};
```

或通过管理后台界面设置。

## 数据积累建议

1. **初期阶段**（数据不足）
   - 系统会使用全局最佳时间
   - 建议先在小范围用户中启用

2. **成长阶段**（1-2周后）
   - 部分用户有足够数据
   - 可以逐步扩大范围

3. **成熟阶段**（1个月后）
   - 大部分用户有足够数据
   - 优化效果开始显现

## 监控和优化

### 查看效果
定期对比启用前后的指标：
- 消息打开率
- 消息点击率  
- 业务转化率

### 调整策略
根据业务目标调整优化策略：
- 关注触达：使用 `OPEN_RATE` 策略
- 关注互动：使用 `CLICK_RATE` 策略
- 关注转化：使用 `CONVERSION_RATE` 策略
- 综合优化：使用 `COMPREHENSIVE` 策略

### 调整权重
根据业务重要性调整综合评分权重：

```properties
# 例如：更重视转化率
austin.smart.send.time.weight.conversion-rate-weight=0.5
austin.smart.send.time.weight.open-rate-weight=0.2
austin.smart.send.time.weight.click-rate-weight=0.2
austin.smart.send.time.weight.activity-weight=0.1
```

## 常见问题

### Q1: 为什么返回的最佳时间是 null？
**A:** 可能的原因：
1. 功能未启用（检查配置 `austin.smart.send.time.enabled`）
2. 用户数据不足且未启用全局降级
3. 时间窗口限制过严

### Q2: 如何加快数据积累？
**A:** 
1. 确保 `BehaviorDataCollector` 正常运行
2. 检查 Kafka 埋点数据是否正常上报
3. 可以手动导入历史数据

### Q3: 全局最佳时间从哪里来？
**A:** 
从所有用户的聚合数据中计算得出，需要一定时间的数据积累。

### Q4: 优化效果不明显怎么办？
**A:**
1. 检查数据质量和样本量
2. 尝试调整优化策略
3. 调整时间窗口设置
4. 考虑是否需要更细粒度的分析（如区分工作日/节假日）

## 下一步

- 查看完整文档：[SMART_SEND_TIME.md](SMART_SEND_TIME.md)
- 了解配置选项：[smart-send-time-config.properties](smart-send-time-config.properties)
- 参与优化：根据业务场景扩展功能
