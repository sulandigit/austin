# Austin 消息推送平台代码优化方案

## 一、性能优化

### 1. 线程池配置优化

**当前问题：**
- 线程池核心线程数和最大线程数都设置为2，过于保守
- 队列大小设置为128/1024，可能导致任务堆积

**优化建议：**

#### 1.1 调整线程池参数（ThreadPoolConstant.java）
```java
// 根据 CPU 核心数动态计算
public static final Integer COMMON_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
public static final Integer COMMON_MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
public static final Integer COMMON_QUEUE_SIZE = 512; // 增大队列

// 针对IO密集型任务（如消息发送）
public static final Integer IO_INTENSIVE_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
public static final Integer IO_INTENSIVE_MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 4;
```

#### 1.2 优化拒绝策略
- 当前使用 CallerRunsPolicy，高并发下会阻塞调用线程
- 建议针对不同场景使用不同策略：
  - 核心业务：CallerRunsPolicy（保证不丢失）
  - 非核心业务：DiscardOldestPolicy 或自定义策略（记录日志）

### 2. 批处理优化

**当前问题：**
- AbstractLazyPending 中批处理阈值可能不合理
- 批量大小和时间阈值需要根据实际业务场景调优

**优化建议：**

#### 2.1 动态批处理参数
```java
// 根据消息类型动态调整批处理参数
- 短信：批量50-100条，时间窗口100-200ms
- 邮件：批量20-50条，时间窗口500ms
- Push：批量100-200条，时间窗口100ms
```

#### 2.2 增加批处理监控
- 记录每批处理的消息数量、耗时
- 根据监控数据自动调优参数

### 3. Redis 性能优化

**当前问题：**
- 使用 pipeline 但可能存在大 key 问题
- mGet 操作可能一次获取过多数据

**优化建议：**

#### 3.1 RedisUtils 优化
```java
// 添加批量大小限制
private static final int MAX_PIPELINE_SIZE = 1000;

public void pipelineSetEx(Map<String, String> keyValues, Long seconds) {
    if (keyValues.size() > MAX_PIPELINE_SIZE) {
        // 分批执行
        Lists.partition(new ArrayList<>(keyValues.entrySet()), MAX_PIPELINE_SIZE)
            .forEach(batch -> executePipelineBatch(batch, seconds));
    } else {
        executePipelineBatch(keyValues.entrySet(), seconds);
    }
}
```

#### 3.2 缓存优化
- 对热点账号配置使用本地缓存（Caffeine）+ Redis 二级缓存
- 设置合理的 TTL，避免缓存穿透

### 4. 数据库查询优化

**优化建议：**

#### 4.1 批量查询优化
- 使用 IN 查询时限制数量（建议不超过1000）
- 添加合适的索引
- 考虑使用 MyBatis 批量操作而非 JPA

#### 4.2 分页查询
- 避免深分页，使用游标分页
- 添加索引覆盖查询

## 二、代码质量优化

### 1. 异常处理优化

**当前问题：**
- 多处代码捕获 Exception 过于宽泛
- Thread.currentThread().interrupt() 使用场景不当

**优化建议：**

#### 1.1 精确异常捕获
```java
// 当前代码
catch (Exception e) {
    log.error("...", e);
}

// 优化后
catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    throw new BusinessException("线程中断", e);
} catch (RedisException e) {
    log.error("Redis操作失败", e);
    // 降级处理
} catch (Exception e) {
    log.error("未知异常", e);
}
```

#### 1.2 统一异常处理
- 定义业务异常类层次结构
- 使用 @ControllerAdvice 统一处理

### 2. 资源管理优化

**当前问题：**
- RedisReceiver 中手动管理线程池，容易遗漏关闭
- 多个单例线程池未统一管理

**优化建议：**

#### 2.1 使用 Spring 管理生命周期
```java
@Component
public class RedisReceiver implements MessageReceiver {
    
    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService scheduler() {
        return new ScheduledThreadPoolExecutor(2, 
            new ThreadFactoryBuilder()
                .setNameFormat("redis-receiver-%d")
                .build());
    }
}
```

#### 2.2 统一线程池管理
- 所有线程池注册到 ThreadPoolExecutorShutdownDefinition
- 实现优雅关闭机制

### 3. 并发安全优化

**当前问题：**
- AbstractLazyPending 中 tasks 字段非线程安全
- volatile 使用不当

**优化建议：**

#### 3.1 线程安全集合
```java
// 当前代码
private List<T> tasks = new ArrayList<>();

// 优化后
private final CopyOnWriteArrayList<T> tasks = new CopyOnWriteArrayList<>();
// 或使用同步块保护
private final List<T> tasks = new ArrayList<>();
private final Object lock = new Object();
```

#### 3.2 正确使用 volatile
```java
// 仅用于标志位
private volatile boolean stop = false;

// 复杂对象使用 AtomicReference
private final AtomicReference<PendingState> state = new AtomicReference<>();
```

### 4. 代码复用和简化

**优化建议：**

#### 4.1 提取公共方法
```java
// 多个 Receiver 中的重复代码可以提取到抽象类
public abstract class AbstractMessageReceiver implements MessageReceiver {
    protected void safeConsume(String message, Consumer<String> consumer) {
        if (StringUtils.isBlank(message)) {
            return;
        }
        try {
            consumer.accept(message);
        } catch (Exception e) {
            log.error("消息消费失败: {}", message, e);
            // 可选：重试或发送到死信队列
        }
    }
}
```

#### 4.2 使用函数式编程简化代码
```java
// 当前代码
List<String> keys = new ArrayList<>();
for (String receiver : receivers) {
    keys.add(buildKey(receiver));
}

// 优化后
List<String> keys = receivers.stream()
    .map(this::buildKey)
    .collect(Collectors.toList());
```

### 5. 日志优化

**优化建议：**

#### 5.1 避免字符串拼接
```java
// 当前
log.error("SmsHandler#handler fail:" + e.getMessage());

// 优化后
log.error("SmsHandler#handler fail", e);
```

#### 5.2 使用占位符
```java
log.info("处理消息: messageId={}, channel={}", messageId, channel);
```

#### 5.3 合理使用日志级别
- ERROR: 系统错误，需要立即处理
- WARN: 警告信息，可能影响业务
- INFO: 关键业务流程
- DEBUG: 详细调试信息（生产环境关闭）

## 三、架构优化

### 1. 消息队列选择优化

**优化建议：**

#### 1.1 根据场景选择 MQ
- Kafka: 高吞吐量场景（营销推送）
- RabbitMQ: 需要消息确认和复杂路由
- RocketMQ: 延迟消息和事务消息
- Redis: 轻量级场景或本地测试

#### 1.2 消息积压处理
- 增加消费者数量（动态扩容）
- 批量消费优化
- 降级策略（丢弃低优先级消息）

### 2. 限流和熔断

**优化建议：**

#### 2.1 限流策略优化
- 当前滑动窗口算法性能较好，继续保持
- 添加分布式限流（Sentinel 或自研）
- 针对不同渠道设置不同限流策略

#### 2.2 熔断机制
```java
// 使用 Resilience4j 或 Sentinel
@CircuitBreaker(name = "sms", fallbackMethod = "smsFallback")
public boolean sendSms(SmsParam param) {
    // 发送逻辑
}

private boolean smsFallback(SmsParam param, Throwable t) {
    log.error("短信发送失败，进入降级", t);
    // 存储到数据库，稍后重试
    return false;
}
```

### 3. 监控和可观测性

**优化建议：**

#### 3.1 增加性能指标
- 消息处理速率（TPS）
- 消息处理延迟（P99、P95）
- 线程池队列积压数量
- 缓存命中率

#### 3.2 链路追踪
- 已有 AnchorInfo，可以增强
- 考虑集成 SkyWalking 或 Zipkin

#### 3.3 告警机制
- 消息积压告警
- 错误率告警
- 线程池满载告警

## 四、配置优化

### 1. 外部化配置

**优化建议：**

#### 1.1 敏感信息加密
```properties
# 使用 Jasypt 加密敏感配置
spring.datasource.password=ENC(encrypted_password)
```

#### 1.2 配置分层
- 公共配置：application.properties
- 环境配置：application-{env}.properties
- 动态配置：Apollo/Nacos

### 2. 数据库连接池优化

**当前配置：**
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
```

**优化建议：**
```properties
# 根据实际并发情况调整
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=20
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
# 开启预编译缓存
spring.datasource.hikari.cachePrepStmts=true
spring.datasource.hikari.prepStmtCacheSize=250
spring.datasource.hikari.prepStmtCacheSqlLimit=2048
```

## 五、安全优化

### 1. 输入验证

**优化建议：**

#### 1.1 参数校验
```java
// 使用 Bean Validation
public class TaskInfo {
    @NotNull(message = "接收者不能为空")
    @Size(min = 1, max = 1000, message = "接收者数量1-1000")
    private Set<String> receiver;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;
}
```

#### 1.2 SQL 注入防护
- 使用参数化查询（已做）
- 避免动态拼接 SQL

### 2. 敏感信息脱敏

**优化建议：**

```java
// 日志脱敏
log.info("发送短信到: {}", maskPhone(phone));

private String maskPhone(String phone) {
    return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
}
```

## 六、测试优化

### 1. 单元测试

**优化建议：**

#### 1.1 增加测试覆盖率
- 核心业务逻辑覆盖率 > 80%
- 使用 JaCoCo 生成覆盖率报告

#### 1.2 使用测试框架
```java
@SpringBootTest
class SmsHandlerTest {
    
    @MockBean
    private SmsScript smsScript;
    
    @Autowired
    private SmsHandler smsHandler;
    
    @Test
    void testSendSms() {
        // given
        when(smsScript.send(any())).thenReturn(mockRecords());
        
        // when
        boolean result = smsHandler.handler(mockTaskInfo());
        
        // then
        assertTrue(result);
        verify(smsScript, times(1)).send(any());
    }
}
```

### 2. 性能测试

**优化建议：**

#### 2.1 压测场景
- 峰值流量：10000 QPS
- 持续流量：5000 QPS
- 消息积压场景

#### 2.2 使用 JMeter 或 Gatling
- 编写性能测试脚本
- 定期执行回归测试

## 七、优先级建议

### P0（立即执行）
1. ✅ 修复线程安全问题（AbstractLazyPending）
2. ✅ 优化异常处理，避免吞异常
3. ✅ 增加关键路径日志

### P1（近期执行）
1. 🔶 线程池参数调优
2. 🔶 Redis 批量操作优化
3. 🔶 增加熔断降级机制
4. 🔶 完善监控告警

### P2（中期规划）
1. 🔷 数据库查询优化
2. 🔷 本地缓存引入
3. 🔷 单元测试覆盖

### P3（长期优化）
1. 📘 架构重构（如有必要）
2. 📘 性能压测和调优
3. 📘 文档完善

## 八、性能基准和目标

### 当前估算性能
- 单机处理能力：~1000 QPS
- 消息延迟：P99 < 500ms
- 线程池利用率：~30%

### 优化后目标
- 单机处理能力：5000-10000 QPS
- 消息延迟：P99 < 200ms
- 线程池利用率：60-70%
- 缓存命中率：> 90%

## 九、实施建议

1. **分阶段实施**：优先 P0 > P1 > P2 > P3
2. **灰度发布**：优化后先在测试环境验证，再灰度到生产
3. **监控验证**：每次优化后对比关键指标
4. **文档同步**：更新技术文档和运维手册
5. **团队 Review**：重要优化需要团队 Code Review

## 十、总结

本优化方案从性能、代码质量、架构、安全等多个维度提出了改进建议。建议按照优先级逐步实施，每个阶段都要有明确的目标和验证方式。优化过程中要注意：

1. **数据驱动**：通过监控数据指导优化方向
2. **风险控制**：重要改动需要充分测试和灰度
3. **持续改进**：建立长效的代码审查和优化机制
4. **知识沉淀**：记录优化过程和结果，形成最佳实践

---

**文档版本**: v1.0  
**创建时间**: 2025-10-31  
**适用版本**: austin 0.0.1-SNAPSHOT
