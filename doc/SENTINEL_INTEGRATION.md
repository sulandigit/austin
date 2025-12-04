# Austin Sentinel 集成使用指南

## 1. 概述

本项目已完成 Sentinel 流量治理框架的集成，提供以下能力：
- **流量控制**：QPS 限流、并发线程数限流、排队等待等多种流控模式
- **熔断降级**：基于 RT、异常比例、异常数的熔断策略
- **系统保护**：基于 CPU、Load、平均 RT 等系统指标的自适应保护
- **实时监控**：通过 Sentinel Dashboard 实时查看流量指标和规则配置

## 2. 快速开始

### 2.1 启用 Sentinel

在 `application.properties` 或环境变量中配置：

```properties
# 启用 Sentinel
austin.sentinel.enabled=true

# 配置 Sentinel Dashboard 地址（可选，用于实时监控）
austin.sentinel.dashboard.server=127.0.0.1:8080

# 配置 Nacos 数据源（用于动态规则管理）
austin.sentinel.nacos.server-addr=127.0.0.1:8848
austin.sentinel.nacos.namespace=sentinel
```

### 2.2 部署 Sentinel Dashboard

1. 下载 Sentinel Dashboard jar 包
2. 启动 Dashboard：
```bash
java -jar sentinel-dashboard.jar --server.port=8080
```
3. 访问控制台：http://localhost:8080
4. 默认用户名密码：sentinel/sentinel

## 3. 资源定义与保护

### 3.1 自动保护的资源

以下资源已自动被 Sentinel 保护，无需额外配置：

- **HTTP 接口**：所有 Web 接口自动创建为 Sentinel 资源
- **消息发送服务**：
  - `biz:send:single` - 单条消息发送
  - `biz:send:batch` - 批量消息发送

### 3.2 渠道调用保护

对下游渠道调用使用 `@ChannelSentinelProtection` 注解进行保护：

```java
@ChannelSentinelProtection(
    channelType = "sms",
    supplier = "tencent",
    enableDegrade = true,
    fallbackMethod = "smsFallback"
)
public boolean sendSms(TaskInfo taskInfo) {
    // 短信发送逻辑
}

// 降级方法
public boolean smsFallback(TaskInfo taskInfo) {
    log.warn("SMS channel degraded, task: {}", taskInfo);
    return false;
}
```

### 3.3 自定义资源保护

使用 `@SentinelResource` 注解保护自定义方法：

```java
@SentinelResource(
    value = "myCustomResource",
    blockHandler = "handleBlock",
    fallback = "handleFallback"
)
public void myMethod() {
    // 业务逻辑
}
```

## 4. 规则配置

### 4.1 通过 Nacos 动态配置（推荐）

在 Nacos 中创建以下配置：

**流控规则** - DataId: `austin-sentinel-flow-rules`
```json
[
  {
    "resource": "/send",
    "grade": 1,
    "count": 100,
    "strategy": 0,
    "controlBehavior": 0
  }
]
```

**熔断规则** - DataId: `austin-sentinel-degrade-rules`
```json
[
  {
    "resource": "downstream:sms:default",
    "grade": 1,
    "count": 0.5,
    "timeWindow": 10
  }
]
```

**系统规则** - DataId: `austin-sentinel-system-rules`
```json
[
  {
    "highestCpuUsage": 0.8,
    "avgRt": 3000
  }
]
```

### 4.2 通过 Sentinel Dashboard 配置

1. 启动应用后，在 Dashboard 中找到 `austin` 应用
2. 在左侧菜单选择"流控规则"、"熔断规则"或"系统规则"
3. 点击"新增规则"按钮，填写规则配置
4. 如果启用了 Nacos 数据源，规则会自动同步到 Nacos

### 4.3 规则参数说明

#### 流控规则参数
- `resource`: 资源名称
- `grade`: 限流阈值类型（0=线程数, 1=QPS）
- `count`: 限流阈值
- `strategy`: 流控模式（0=直接, 1=关联, 2=链路）
- `controlBehavior`: 流控效果（0=快速失败, 1=Warm Up, 2=排队等待）

#### 熔断规则参数
- `resource`: 资源名称
- `grade`: 熔断策略（0=慢调用比例, 1=异常比例, 2=异常数）
- `count`: 阈值（慢调用 RT/异常比例/异常数）
- `timeWindow`: 熔断时长（秒）
- `minRequestAmount`: 最小请求数

#### 系统规则参数
- `highestCpuUsage`: 最高 CPU 使用率（0-1）
- `avgRt`: 平均响应时间（毫秒）
- `maxThread`: 最大并发线程数
- `qps`: 最大 QPS
- `highestSystemLoad`: 最高系统 Load

## 5. 监控与告警

### 5.1 查看实时指标

在 Sentinel Dashboard 中可以查看：
- 实时 QPS、RT、线程数
- 资源调用链路
- 规则命中情况
- 系统负载情况

### 5.2 获取监控指标

使用 `SentinelMetricsUtils` 工具类获取指标：

```java
Map<String, Object> metrics = SentinelMetricsUtils.getResourceMetrics("biz:send:single");
System.out.println("QPS: " + metrics.get("passQps"));
System.out.println("Avg RT: " + metrics.get("avgRt"));
```

### 5.3 日志监控

Sentinel 会记录以下日志：
- `[Sentinel] Flow control triggered` - 流控触发
- `[Sentinel] Degrade triggered` - 熔断触发
- `[Sentinel] System protection triggered` - 系统保护触发
- `[ChannelSentinelProtection] Resource blocked` - 渠道保护触发

## 6. 环境配置

### 6.1 开发环境（dev）

```properties
austin.sentinel.enabled=true
# 使用较宽松的规则，便于开发测试
austin.sentinel.system.max-cpu-usage=0.9
```

### 6.2 测试环境（test）

```properties
austin.sentinel.enabled=true
austin.sentinel.dashboard.server=test-sentinel-dashboard:8080
austin.sentinel.nacos.server-addr=test-nacos:8848
```

### 6.3 生产环境（prod）

```properties
austin.sentinel.enabled=true
austin.sentinel.dashboard.server=prod-sentinel-dashboard:8080
austin.sentinel.nacos.server-addr=prod-nacos:8848
# 使用严格的系统保护规则
austin.sentinel.system.max-cpu-usage=0.8
austin.sentinel.system.max-load=8.0
```

## 7. 常见问题

### 7.1 Sentinel 不生效？

检查以下配置：
1. `austin.sentinel.enabled=true` 是否已设置
2. 依赖是否正确引入
3. 应用是否已连接到 Sentinel Dashboard

### 7.2 规则不生效？

1. 检查 Nacos 配置是否正确
2. 查看应用日志是否有规则加载成功的日志
3. 在 Dashboard 中确认规则是否已下发

### 7.3 如何禁用 Sentinel？

设置 `austin.sentinel.enabled=false` 即可，所有 Sentinel 组件不会初始化。

### 7.4 与现有限流的关系？

- Sentinel 主要保护入口和下游调用
- 现有的 Guava + Redis 限流继续保留，用于业务细粒度控制
- 两者不冲突，按职责分工使用

## 8. 最佳实践

1. **规则配置原则**
   - 入口接口限流阈值 > 业务服务限流阈值 > 下游渠道限流阈值
   - 先在测试环境验证规则，再发布到生产
   - 重要规则变更需要走变更流程

2. **熔断策略**
   - 对下游不稳定的渠道设置熔断规则
   - 熔断时长不宜过长（建议 10-30 秒）
   - 设置合理的最小请求数，避免误熔断

3. **监控告警**
   - 关注系统保护规则的触发频率
   - 监控各渠道的熔断情况
   - 对核心资源设置告警阈值

4. **降级预案**
   - 为关键渠道配置降级方法
   - 降级时记录详细日志
   - 定期演练降级流程

## 9. 参考资料

- [Sentinel 官方文档](https://sentinelguard.io/zh-cn/docs/introduction.html)
- [Sentinel Dashboard 部署指南](https://sentinelguard.io/zh-cn/docs/dashboard.html)
- [Nacos 数据源配置](https://sentinelguard.io/zh-cn/docs/dynamic-rule-configuration.html)

## 10. 规则示例文件

项目中提供了示例规则文件，位于：
- `austin-web/src/main/resources/sentinel/flow-rules-example.json`
- `austin-web/src/main/resources/sentinel/degrade-rules-example.json`
- `austin-web/src/main/resources/sentinel/system-rules-example.json`

可参考这些文件配置 Nacos 规则。
