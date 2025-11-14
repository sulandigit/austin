# 智能发送时间优化功能

## 功能概述

智能发送时间优化功能基于用户历史行为数据，分析用户在不同时间段对消息的打开率、点击率和转化率，自动为每个用户推荐最佳的消息发送时间，从而提升消息的触达效果。

## 核心特性

1. **基于用户行为分析**：收集和分析用户在不同时间段的消息互动数据
2. **多维度优化策略**：支持基于打开率、点击率、转化率或综合评分的优化
3. **智能降级机制**：用户数据不足时自动使用全局最佳时间
4. **灵活配置**：支持时间窗口限制、最小样本量等多种配置
5. **A/B测试支持**：可开启A/B测试验证优化效果

## 架构设计

### 核心组件

1. **UserBehaviorStats（用户行为统计实体）**
   - 存储用户在不同时间段的消息互动数据
   - 包含发送数、打开数、点击数、转化数等统计指标

2. **UserBehaviorAnalysisService（用户行为分析服务）**
   - 收集和更新用户行为统计数据
   - 计算各项指标（打开率、点击率、转化率）
   - 计算用户活跃度评分

3. **SmartSendTimeOptimizer（智能发送时间优化服务）**
   - 根据用户行为数据推荐最佳发送时间
   - 支持多种优化策略
   - 提供批量查询接口

4. **BehaviorDataCollector（行为数据收集器）**
   - 监听消息埋点数据
   - 实时收集用户行为信息
   - 异步更新统计数据

## 使用指南

### 1. 数据库初始化

执行 `doc/sql/smart_send_time.sql` 文件，创建必要的数据表：

```sql
-- 创建用户行为统计表
source doc/sql/smart_send_time.sql;
```

### 2. 配置文件设置

在 `application.properties` 中添加以下配置：

```properties
# 智能发送时间优化配置
# 是否启用智能发送时间优化
austin.smart.send.time.enabled=true

# 优化策略：OPEN_RATE（打开率）、CLICK_RATE（点击率）、CONVERSION_RATE（转化率）、COMPREHENSIVE（综合评分）
austin.smart.send.time.strategy=COMPREHENSIVE

# 最小数据样本量（用户历史数据不足时使用全局数据）
austin.smart.send.time.min-sample-size=10

# 全局数据最小样本量
austin.smart.send.time.global-min-sample-size=100

# 推荐时间段数量
austin.smart.send.time.recommend-time-slots=3

# 数据有效期（天）
austin.smart.send.time.data-valid-days=90

# 是否允许使用全局最佳时间（当用户数据不足时）
austin.smart.send.time.allow-global-best-time=true

# 综合评分权重配置
austin.smart.send.time.weight.open-rate-weight=0.4
austin.smart.send.time.weight.click-rate-weight=0.3
austin.smart.send.time.weight.conversion-rate-weight=0.2
austin.smart.send.time.weight.activity-weight=0.1

# 时间窗口配置（允许的发送时间范围）
austin.smart.send.time.time-window.enabled=true
austin.smart.send.time.time-window.earliest-hour=8
austin.smart.send.time.time-window.latest-hour=22

# A/B测试配置
austin.smart.send.time.enable-ab-test=false
austin.smart.send.time.ab-test-ratio=20
```

### 3. 在模板中启用智能发送时间

创建或编辑消息模板时，设置以下字段：

- `enableSmartSendTime`: 设置为 1 启用智能发送时间优化
- `smartSendTimeStrategy`: 选择优化策略（OPEN_RATE、CLICK_RATE、CONVERSION_RATE、COMPREHENSIVE）

### 4. API 使用示例

#### 4.1 获取用户最佳发送时间

```java
@Autowired
private SmartSendTimeOptimizer smartSendTimeOptimizer;

// 获取单个用户的最佳发送时间
Integer bestHour = smartSendTimeOptimizer.getBestSendTime("user@example.com", 40);
System.out.println("最佳发送时间：" + bestHour + "点");

// 获取推荐的前3个时间段
List<SmartSendTimeResult> recommendations = smartSendTimeOptimizer.getRecommendedSendTimes(
    "user@example.com", 40, 3);
for (SmartSendTimeResult result : recommendations) {
    System.out.println(String.format("推荐时间：%d点，打开率：%.2f%%，置信度：%.2f%%",
        result.getHourOfDay(), result.getPredictedOpenRate(), result.getConfidence()));
}
```

#### 4.2 批量获取用户最佳发送时间

```java
List<String> receivers = Arrays.asList("user1@example.com", "user2@example.com", "user3@example.com");
List<SmartSendTimeResult> results = smartSendTimeOptimizer.batchGetBestSendTime(receivers, 40);
```

#### 4.3 优化发送时间

```java
// 将原始发送时间调整为用户的最佳发送时间
Long originalTime = System.currentTimeMillis();
Long optimizedTime = smartSendTimeOptimizer.optimizeSendTime("user@example.com", 40, originalTime);
```

#### 4.4 手动更新用户行为统计

```java
@Autowired
private UserBehaviorAnalysisService userBehaviorAnalysisService;

// 用户打开了消息
userBehaviorAnalysisService.updateBehaviorStats(
    "user@example.com",  // 接收者
    40,                   // 邮件渠道
    10,                   // 10点钟
    true,                 // 已打开
    false,                // 未点击
    false                 // 未转化
);

// 用户点击了消息
userBehaviorAnalysisService.updateBehaviorStats(
    "user@example.com", 40, 10, true, true, false);
```

## 优化策略说明

### 1. OPEN_RATE（打开率优先）
优先选择用户打开率最高的时间段，适用于关注消息触达率的场景。

### 2. CLICK_RATE（点击率优先）
优先选择用户点击率最高的时间段，适用于关注用户互动的场景。

### 3. CONVERSION_RATE（转化率优先）
优先选择转化率最高的时间段，适用于关注业务转化的场景。

### 4. COMPREHENSIVE（综合评分）
综合考虑打开率、点击率、转化率和活跃度，根据配置的权重计算综合评分，适用于大多数场景。

**综合评分计算公式：**
```
综合评分 = 打开率 × 0.4 + 点击率 × 0.3 + 转化率 × 0.2 + 活跃度 × 0.1
```

## 数据收集机制

### 自动收集
系统通过 `BehaviorDataCollector` 监听 Kafka 消息埋点数据，自动收集用户行为：

- **发送成功**：记录消息发送和打开（简化处理）
- **点击事件**：记录用户点击行为
- **转化事件**：需要业务方额外标识

### 数据更新流程

```
消息发送 → 埋点上报 → Kafka → BehaviorDataCollector → UserBehaviorAnalysisService → 数据库
```

## 智能降级机制

当用户历史数据不足时，系统会自动降级使用全局最佳时间：

1. 用户样本量 < minSampleSize：使用全局数据
2. 全局数据不可用：使用默认时间（上午10点）
3. 可通过配置 `allowGlobalBestTime` 控制是否启用降级

## 性能优化建议

1. **定期清理过期数据**
   ```java
   // 清理90天前的数据
   userBehaviorAnalysisService.cleanExpiredData(90);
   ```

2. **批量查询**
   - 对于大量用户，使用 `batchGetBestSendTime` 批量查询

3. **缓存优化**
   - 可在服务层增加缓存，减少数据库查询

4. **异步处理**
   - 行为数据收集已使用 Kafka 异步处理

## 监控指标

建议监控以下指标：

1. **数据覆盖率**：有行为数据的用户占比
2. **优化效果**：启用前后的打开率、点击率对比
3. **降级比例**：使用全局数据的用户占比
4. **数据延迟**：行为数据更新的延迟时间

## 注意事项

1. 需要一定时间的数据积累才能达到较好的优化效果
2. 定期清理过期数据，避免数据量过大影响性能
3. 根据业务特点调整权重配置和时间窗口
4. 建议先在小范围用户中测试，验证效果后再全量推广
5. 行为数据收集器需要根据实际的埋点数据结构进行调整

## 未来优化方向

1. 支持工作日/节假日的差异化时间推荐
2. 支持用户分群的差异化策略
3. 引入机器学习模型提升预测准确度
4. 支持更细粒度的时间推荐（分钟级）
5. 提供可视化的数据分析面板
