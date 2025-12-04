# 慢SQL监控功能使用说明

## 功能概述

慢SQL监控功能通过动态代理的方式拦截数据库连接,监控SQL执行时间,当SQL执行时间超过设定的阈值时,自动记录慢SQL日志。

## 实现原理

1. **数据源包装**: 通过`SlowSqlDataSource`包装原始数据源
2. **连接代理**: 通过`SlowSqlConnectionProxy`代理Connection对象
3. **Statement代理**: 通过`SlowSqlStatementProxy`代理Statement和PreparedStatement
4. **时间监控**: 在SQL执行前后记录时间戳,计算执行耗时
5. **日志记录**: 当执行时间超过阈值时,记录WARN级别的日志

## 功能特点

- ✅ 无侵入式监控,不需要修改业务代码
- ✅ 基于动态代理实现,性能损耗极小
- ✅ 支持自定义慢SQL阈值
- ✅ 支持启用/禁用功能
- ✅ 自动记录SQL语句和执行时间
- ✅ 可扩展告警功能

## 配置说明

在`application.properties`中配置:

```properties
# 是否启用慢SQL监控(默认true)
austin.slow.sql.enabled=true

# 慢SQL阈值,单位毫秒(默认1000ms,即1秒)
austin.slow.sql.threshold=1000

# 是否打印SQL参数(默认true)
austin.slow.sql.print-parameters=true

# 是否启用告警(默认false)
austin.slow.sql.alert-enabled=false

# 告警阈值,同一SQL在指定时间窗口内出现多少次慢查询时触发告警(默认10次)
austin.slow.sql.alert-threshold=10

# 告警时间窗口,单位分钟(默认5分钟)
austin.slow.sql.alert-time-window=5
```

## 日志示例

当检测到慢SQL时,会输出如下格式的日志:

```
2025-12-04 10:30:15.123 WARN  [http-nio-8080-exec-1] c.j.a.common.monitor.SlowSqlStatementProxy : 【慢SQL监控】检测到慢SQL, 执行时间: 1523ms, SQL: SELECT * FROM message_template WHERE id = 1
```

## 使用场景

1. **开发阶段**: 及时发现性能问题,优化SQL语句
2. **测试阶段**: 进行性能测试,定位慢SQL
3. **生产环境**: 实时监控数据库性能,快速定位问题

## 扩展功能

在`SlowSqlStatementProxy.logSlowSql()`方法中可以扩展以下功能:

1. **告警通知**: 发送邮件、短信、企业微信等告警
2. **数据统计**: 记录慢SQL到数据库,便于后续分析
3. **监控集成**: 推送到Prometheus、Grafana等监控系统
4. **性能分析**: 统计慢SQL的次数、平均耗时等指标

## 注意事项

1. 慢SQL阈值应根据业务场景合理设置,避免误报
2. 生产环境建议保持监控功能启用,阈值可适当调高
3. 如需详细的SQL执行计划分析,建议结合MySQL的慢查询日志
4. 监控功能会有微小的性能开销,但相比收益可以忽略不计

## 技术架构

```
SlowSqlDataSource (数据源包装层)
    ↓
SlowSqlConnectionProxy (连接代理层)
    ↓
SlowSqlStatementProxy (Statement代理层)
    ↓
实际SQL执行 + 时间监控
```

## 禁用监控

如果需要临时禁用慢SQL监控,可以设置:

```properties
austin.slow.sql.enabled=false
```

或者注释掉相关配置,系统会使用原始的数据源。
