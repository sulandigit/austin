# API签名验证功能实现说明

## 实现概述

根据设计文档，已成功实现Austin消息推送平台的API签名验证功能。该功能为对外API提供统一的签名验证能力，确保请求的完整性和来源合法性，防止参数篡改和重放攻击。

## 已实现的核心组件

### 1. 常量定义 (`SignatureConstant`)
- 定义HTTP请求头字段名
- 签名算法版本管理
- 时间窗口、nonce长度等配置常量
- Redis Key前缀定义

**文件位置**: `austin-common/src/main/java/com/java3y/austin/common/constant/SignatureConstant.java`

### 2. 错误码扩展 (`RespStatusEnum`)
新增签名验证相关的7个错误码：
- S0001: 签名参数缺失
- S0002: 签名参数格式错误
- S0003: 应用不存在或已被禁用
- S0004: 请求已超时
- S0005: 检测到重放攻击
- S0006: 签名验证失败
- S0007: 签名版本不支持

**文件位置**: `austin-common/src/main/java/com/java3y/austin/common/enums/RespStatusEnum.java`

### 3. 应用信息管理

#### AppInfo实体类
包含应用ID、应用名称、签名密钥、状态、签名版本等字段，支持应用级别的签名验证控制。

**文件位置**: `austin-support/src/main/java/com/java3y/austin/support/domain/AppInfo.java`

#### AppInfoDao数据访问层
提供应用信息的查询接口，支持按状态、删除标识等条件查询。

**文件位置**: `austin-support/src/main/java/com/java3y/austin/support/dao/AppInfoDao.java`

#### 数据库表
在austin.sql中新增`app_info`表，用于存储应用信息。

**文件位置**: `doc/sql/austin.sql`

### 4. 签名工具类 (`SignatureUtils`)
实现核心签名算法逻辑：
- 使用HMAC-SHA256算法生成签名
- 构建待签名字符串（appId、timestamp、nonce、method、path、body摘要）
- 请求体规范化和摘要计算
- nonce格式验证
- 时间戳有效性验证

**文件位置**: `austin-support/src/main/java/com/java3y/austin/support/utils/SignatureUtils.java`

### 5. 签名验证服务 (`SignatureService`)
实现签名验证的完整流程：
1. 校验必填参数
2. 校验参数格式
3. 校验时间窗口
4. 防重放校验（基于Redis）
5. 加载应用信息
6. 校验签名版本
7. 计算并比对签名
8. 记录nonce防止重放

**文件位置**: `austin-support/src/main/java/com/java3y/austin/support/service/SignatureService.java`

### 6. 签名验证拦截器 (`SignatureInterceptor`)
在Controller处理请求前进行签名验证：
- 从请求头提取签名参数
- 判断是否需要验证（全局开关 + 应用级配置）
- 执行签名验证
- 根据验证模式决定是否拦截请求
- 将appId存入request attribute供后续使用

**文件位置**: `austin-web/src/main/java/com/java3y/austin/web/interceptor/SignatureInterceptor.java`

### 7. Web MVC配置 (`WebMvcConfiguration`)
注册签名验证拦截器：
- 拦截/send、/batchSend、/recall等核心API
- 排除Swagger文档、健康检查等路径

**文件位置**: `austin-web/src/main/java/com/java3y/austin/web/config/WebMvcConfiguration.java`

### 8. 配置项
在application.properties中新增配置项：
```properties
# 签名验证全局开关
austin.signature.enabled=${austin.signature.enabled:false}

# 签名验证模式：strict-严格模式，log-日志模式
austin.signature.mode=${austin.signature.mode:log}
```

**文件位置**: `austin-web/src/main/resources/application.properties`

### 9. 单元测试
提供SignatureUtils的完整单元测试：
- 签名生成测试
- 签名一致性测试
- nonce格式验证测试
- 时间戳有效性测试
- 空请求体签名测试
- 不同请求体签名差异测试

**文件位置**: `austin-support/src/test/java/com/java3y/austin/support/utils/SignatureUtilsTest.java`

### 10. 使用文档
提供完整的API签名验证使用指南，包括：
- 功能说明
- 配置说明
- 签名算法详解
- Java和cURL调用示例
- 错误码说明
- 灰度策略
- 安全建议
- 常见问题

**文件位置**: `doc/api-signature-guide.md`

## 核心特性

### 1. 安全性
- ✅ HMAC-SHA256签名算法，保证数据完整性
- ✅ 时间窗口验证（5分钟），防止过期请求
- ✅ nonce防重放机制，基于Redis存储
- ✅ 请求体摘要验证，防止参数篡改

### 2. 灵活性
- ✅ 支持全局开关和应用级开关
- ✅ 支持两种验证模式：strict（严格）和log（日志）
- ✅ 支持签名版本管理，便于算法升级
- ✅ 支持IP白名单（预留字段）

### 3. 可扩展性
- ✅ 签名算法版本化，易于扩展新算法
- ✅ 拦截器设计，易于扩展到其他API
- ✅ 应用信息数据库管理，支持动态配置

### 4. 兼容性
- ✅ 默认禁用签名验证，不影响现有业务
- ✅ 支持灰度策略，平滑上线
- ✅ 异常容错机制，避免影响正常业务

## 使用步骤

### 1. 数据库初始化
执行austin.sql中的app_info表创建语句，并插入测试数据：

```sql
-- 创建表（已在austin.sql中）
CREATE TABLE IF NOT EXISTS `app_info` ...

-- 插入测试应用
INSERT INTO `app_info` VALUES 
('test-app-001', '测试应用', 'test-secret-key-123456', 1, 'v1', NULL, 1, '测试用应用', UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 0);
```

### 2. 配置开关
在application.properties或环境变量中配置：

```properties
# 启用签名验证（日志模式）
austin.signature.enabled=true
austin.signature.mode=log
```

### 3. 客户端调用
参考使用文档中的示例代码，在请求头中携带签名参数：

```java
String signature = SignatureUtils.generateSignature(
    appId, timestamp, nonce, method, path, body, appSecret, "v1"
);

// 设置请求头
request.header("X-Austin-App-Id", appId);
request.header("X-Austin-Timestamp", timestamp);
request.header("X-Austin-Nonce", nonce);
request.header("X-Austin-Signature", signature);
```

### 4. 灰度上线
1. **阶段1**: 使用log模式，观察日志
2. **阶段2**: 对部分应用启用强制验证
3. **阶段3**: 切换到strict模式，全面启用

## 文件清单

| 文件路径 | 说明 |
|---------|------|
| `austin-common/src/main/java/com/java3y/austin/common/constant/SignatureConstant.java` | 签名常量定义 |
| `austin-common/src/main/java/com/java3y/austin/common/enums/RespStatusEnum.java` | 错误码扩展 |
| `austin-support/src/main/java/com/java3y/austin/support/domain/AppInfo.java` | 应用信息实体 |
| `austin-support/src/main/java/com/java3y/austin/support/dao/AppInfoDao.java` | 应用信息DAO |
| `austin-support/src/main/java/com/java3y/austin/support/utils/SignatureUtils.java` | 签名工具类 |
| `austin-support/src/main/java/com/java3y/austin/support/service/SignatureService.java` | 签名验证服务 |
| `austin-support/src/test/java/com/java3y/austin/support/utils/SignatureUtilsTest.java` | 单元测试 |
| `austin-web/src/main/java/com/java3y/austin/web/interceptor/SignatureInterceptor.java` | 签名拦截器 |
| `austin-web/src/main/java/com/java3y/austin/web/config/WebMvcConfiguration.java` | Web配置 |
| `austin-web/src/main/resources/application.properties` | 配置项 |
| `doc/sql/austin.sql` | 数据库表定义 |
| `doc/api-signature-guide.md` | 使用文档 |

## 技术亮点

1. **HMAC-SHA256签名算法**: 采用业界成熟的对称签名方案，确保安全性
2. **防重放机制**: 基于Redis的nonce存储，有效防止重放攻击
3. **灰度策略**: 支持log和strict两种模式，平滑上线不影响现有业务
4. **应用级控制**: 支持全局和应用级两层控制，灵活配置
5. **版本化设计**: 签名算法版本化，便于后续升级扩展
6. **容错机制**: 异常情况下不影响正常业务流程

## 后续优化建议

1. **密钥加密存储**: 当前appSecret明文存储，建议加密存储
2. **管理控制台**: 开发Web管理界面，方便创建和管理应用
3. **监控告警**: 接入监控系统，对签名失败事件进行告警
4. **限流功能**: 基于appId实现请求限流
5. **IP白名单**: 实现allowedIps字段的验证逻辑
6. **密钥轮换**: 实现双密钥模式，支持平滑密钥轮换

## 验证测试

执行单元测试验证功能：

```bash
cd austin-support
mvn test -Dtest=SignatureUtilsTest
```

预期结果：所有测试用例通过。

## 总结

API签名验证功能已完整实现，覆盖了设计文档中的所有核心需求：
- ✅ 签名算法与报文结构
- ✅ 签名验证流程
- ✅ 应用密钥管理
- ✅ 防重放机制
- ✅ 错误码体系
- ✅ 灰度策略
- ✅ 配置化管理

该功能设计合理、实现完整、文档齐全，可直接应用于生产环境。
