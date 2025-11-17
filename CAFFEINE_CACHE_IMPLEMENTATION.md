# Caffeine Cache 替代 ConcurrentHashMap 实施总结

## 实施概况

本次改造成功将 Austin 消息推送平台中的部分 ConcurrentHashMap 替换为 Caffeine Cache，提升了缓存管理能力和系统性能。

## 完成的工作

### 1. 依赖引入

#### 父 pom.xml
- 添加 Caffeine 版本属性：`caffeine.version = 2.9.3`
- 在 dependencyManagement 中声明 Caffeine 依赖

#### 子模块 pom.xml
- austin-handler 模块：添加 Caffeine 依赖
- austin-support 模块：添加 Caffeine 依赖

### 2. 工具类创建

创建了 `CaffeineUtils` 工具类（位于 austin-support 模块），提供：

#### 预设缓存模板
- `buildConfigCache()`: 配置缓存模板
  - 最大容量 1000
  - 写入后 30 分钟过期
  - 启用统计
  
- `buildObjectCache()`: 对象缓存模板
  - 最大容量 500
  - 访问后 1 小时过期
  - 写入后 30 分钟刷新
  - 启用弱值引用
  - 启用统计

- `buildCustomCache()`: 自定义配置缓存

#### 统计工具方法
- `logCacheStats()`: 输出缓存统计日志
- `formatCacheStats()`: 格式化缓存统计信息
- `logCacheInit()`: 输出缓存初始化日志

### 3. AccountUtils 改造

#### 改造内容
- 将 `officialAccountServiceMap` (ConcurrentHashMap) 替换为 `officialAccountServiceCache` (LoadingCache)
- 将 `miniProgramServiceMap` (ConcurrentHashMap) 替换为 `miniProgramServiceCache` (LoadingCache)

#### 缓存配置
- 使用 `CaffeineUtils.buildObjectCache()` 创建缓存实例
- 配置特性：
  - 最大容量：500
  - 访问后过期：1 小时
  - 写入后刷新：30 分钟
  - 弱值引用：是
  - 统计功能：启用

#### 自动加载机制
- 公众号服务：自动从数据库加载账号配置并初始化 WxMpService
- 小程序服务：自动从数据库加载账号配置并初始化 WxMaService
- 异常处理：加载失败返回 null，记录错误日志

#### 新增方法
- `refreshAccountCache(Integer sendAccountId)`: 手动刷新指定账号缓存
- `getCacheStats()`: 获取缓存统计信息

### 4. ChannelAccount 优化

优化了 ChannelAccount 实体类的 equals 和 hashCode 实现：
- 添加 `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` 注解
- 仅基于 `id` 字段进行比较（添加 `@EqualsAndHashCode.Include` 注解）
- 确保缓存键的唯一性和一致性

## 改造效果

### 功能增强
1. **自动过期机制**：缓存数据会自动失效，防止使用陈旧配置
2. **容量限制**：设置最大容量，避免内存无限增长
3. **自动刷新**：写入 30 分钟后自动后台刷新，避免请求阻塞
4. **弱引用**：内存紧张时可自动回收，防止内存泄漏
5. **缓存统计**：提供命中率、加载耗时等监控指标

### 性能优化
- Caffeine 采用 W-TinyLFU 淘汰算法，性能优于传统 LRU
- 异步刷新机制，避免用户请求等待
- 高并发场景下读性能更优

### 可观测性提升
- 初始化时输出缓存配置日志
- 支持获取实时缓存统计信息
- 便于监控和问题诊断

## 未改造的场景

以下场景保留使用 ConcurrentHashMap，原因说明：

1. **FlowControlFactory.flowControlServiceMap**
   - 存储 Spring Bean 引用，生命周期由 Spring 容器管理
   - 应用启动时一次性加载，运行期间不变
   - 数据量极小且固定

2. **ServiceLoadBalancerFactory.serviceLoadBalancerMap**
   - 存储 Spring Bean 引用
   - 应用启动时初始化，运行期间不变

3. **DeduplicationHolder**（builderHolder 和 serviceHolder）
   - 存储 Spring Bean 引用
   - 属于注册表模式，不是缓存场景
   - 数据量极小（仅 2 种去重类型）

## 使用示例

### 获取账号服务（自动缓存）
```java
@Autowired
private AccountUtils accountUtils;

// 自动使用 Caffeine 缓存，未命中时自动加载
WxMpService wxMpService = accountUtils.getAccountById(sendAccountId, WxMpService.class);
```

### 手动刷新缓存
```java
// 刷新指定账号的缓存
accountUtils.refreshAccountCache(sendAccountId);
```

### 查看缓存统计
```java
// 获取缓存统计信息
String stats = accountUtils.getCacheStats();
log.info("Account cache stats: {}", stats);
```

## 验证结果

- ✅ 所有依赖正确引入
- ✅ CaffeineUtils 工具类创建成功
- ✅ AccountUtils 改造完成，保持原有业务逻辑
- ✅ ChannelAccount equals/hashCode 优化完成
- ✅ 所有文件编译通过，无语法错误

## 后续优化建议

### 1. 配置中心集成（如果存在 RateLimitConfigManager）
如果后续实现了 RateLimitConfigManager，建议：
- 监听配置中心变更事件
- 配置变更时主动刷新缓存
- 实现配置实时生效

### 2. 监控集成
- 将缓存统计指标集成到 Prometheus
- 配置缓存命中率告警（如 < 80%）
- 监控缓存加载耗时

### 3. 分布式缓存一致性
- 使用消息队列广播缓存刷新事件
- 多实例缓存数据保持一致

### 4. 缓存预热
- 应用启动时预加载热点账号
- 减少冷启动时的缓存未命中

### 5. 动态配置
- 缓存参数可通过配置中心动态调整
- 无需重启应用即可调优

## 注意事项

1. **弱引用的影响**
   - 弱值引用可能导致缓存对象被频繁 GC 回收
   - 如果淘汰率过高，可调整为软引用或强引用
   - 建议监控缓存淘汰率和加载耗时

2. **ChannelAccount 的 equals/hashCode**
   - 缓存键基于 ChannelAccount.id 字段
   - 确保 id 字段在对象生命周期内不变
   - 不要使用临时对象作为缓存键

3. **缓存刷新**
   - 异步刷新时会返回旧值
   - 如果需要强制同步刷新，可调用 refreshAccountCache

4. **异常处理**
   - 加载失败会返回 null
   - 业务代码需要处理 null 情况
   - 避免 NPE

## 参考资料

- [Caffeine 官方文档](https://github.com/ben-manes/caffeine/wiki)
- [Caffeine 性能测试](https://github.com/ben-manes/caffeine/wiki/Benchmarks)
- [设计文档](/data/.task/design.md)
