# Austin 单元测试文档

## 概述

本文档描述了为 Austin 消息推送平台添加的单元测试。

## 已添加的测试模块

### 1. austin-common 模块

#### 1.1 EnumUtilTest
**位置**: `austin-common/src/test/java/com/java3y/austin/common/enums/EnumUtilTest.java`

**测试内容**:
- `testGetDescriptionByCode()` - 测试根据code获取枚举描述
- `testGetEnumByCode()` - 测试根据code获取枚举对象
- `testGetCodeList()` - 测试获取枚举code列表
- `testGetCodeListForDeduplicationType()` - 测试去重类型枚举的code列表

**覆盖场景**:
- 正常获取枚举信息
- 不存在的code返回null或空字符串
- 多种枚举类型测试

#### 1.2 ProcessControllerTest
**位置**: `austin-common/src/test/java/com/java3y/austin/common/pipeline/ProcessControllerTest.java`

**测试内容**:
- `testProcessSuccess()` - 测试责任链正常执行
- `testProcessWithBreak()` - 测试责任链中断机制
- `testProcessWithNullContext()` - 测试上下文为null
- `testProcessWithNullBusinessCode()` - 测试业务代码为null
- `testProcessWithNullTemplate()` - 测试模板不存在
- `testProcessWithEmptyProcessList()` - 测试处理器列表为空

**覆盖场景**:
- 责任链完整执行流程
- 责任链中断逻辑
- 各种异常情况的前置检查

#### 1.3 BasicResultVOTest
**位置**: `austin-common/src/test/java/com/java3y/austin/common/vo/BasicResultVOTest.java`

**测试内容**:
- `testSuccessWithoutData()` - 测试无数据的成功响应
- `testSuccessWithMessage()` - 测试带自定义消息的成功响应
- `testSuccessWithData()` - 测试带数据的成功响应
- `testFailWithoutParams()` - 测试默认失败响应
- `testFailWithMessage()` - 测试带自定义错误消息的失败响应
- `testFailWithStatus()` - 测试带自定义状态的失败响应
- `testFailWithStatusAndMessage()` - 测试带自定义状态和消息的失败响应
- `testSuccessWithComplexData()` - 测试复杂对象数据

**覆盖场景**:
- 各种成功和失败响应构造方式
- 带数据和不带数据的响应
- 自定义状态和消息

### 2. austin-support 模块

#### 2.1 ContentHolderUtilTest
**位置**: `austin-support/src/test/java/com/java3y/austin/support/utils/ContentHolderUtilTest.java`

**测试内容**:
- `testReplacePlaceHolderSuccess()` - 测试正常占位符替换
- `testReplacePlaceHolderWithMultipleOccurrences()` - 测试多次出现同一占位符
- `testReplacePlaceHolderWithNoPlaceholder()` - 测试无占位符情况
- `testReplacePlaceHolderWithNullParamMap()` - 测试参数映射为null
- `testReplacePlaceHolderWithMissingParam()` - 测试缺少必需参数
- `testReplacePlaceHolderWithEmptyValue()` - 测试空值参数
- `testReplacePlaceHolderWithComplexContent()` - 测试复杂内容替换
- `testReplacePlaceHolderWithSpecialCharacters()` - 测试特殊字符
- `testReplacePlaceHolderWithChineseCharacters()` - 测试中文字符

**覆盖场景**:
- 正常占位符替换
- 异常参数处理
- 特殊字符和中文支持

### 3. austin-service-api-impl 模块

#### 3.1 SendPreCheckActionTest
**位置**: `austin-service-api-impl/src/test/java/com/java3y/austin/service/api/impl/action/send/SendPreCheckActionTest.java`

**测试内容**:
- `testProcessWithValidData()` - 测试有效数据处理
- `testProcessWithNullTemplateId()` - 测试模板ID为null
- `testProcessWithEmptyMessageParamList()` - 测试空参数列表
- `testProcessWithBlankReceiver()` - 测试接收者为空
- `testProcessWithMixedReceivers()` - 测试混合有效和无效接收者
- `testProcessWithTooManyReceivers()` - 测试接收者超过100个
- `testProcessWithExactly100Receivers()` - 测试正好100个接收者

**覆盖场景**:
- 参数校验逻辑
- 接收者过滤
- 接收者数量限制

#### 3.2 SendServiceImplCompleteTest
**位置**: `austin-service-api-impl/src/test/java/com/java3y/austin/service/api/impl/service/SendServiceImplCompleteTest.java`

**测试内容**:
- `testSendWithValidRequest()` - 测试有效的发送请求
- `testSendWithNullRequest()` - 测试空请求
- `testBatchSendWithValidRequest()` - 测试有效的批量发送请求
- `testBatchSendWithNullRequest()` - 测试批量发送空请求
- `testSendWithProcessFailure()` - 测试处理失败情况
- `testBatchSendWithEmptyList()` - 测试空列表批量发送
- `testBatchSendWithMultipleParams()` - 测试多个参数批量发送

**覆盖场景**:
- 单条消息发送
- 批量消息发送
- 空请求处理
- 处理失败场景

#### 3.3 RecallServiceImplTest
**位置**: `austin-service-api-impl/src/test/java/com/java3y/austin/service/api/impl/service/RecallServiceImplTest.java`

**测试内容**:
- `testRecallWithValidRequest()` - 测试有效的撤回请求
- `testRecallWithNullRequest()` - 测试空请求
- `testRecallWithSingleMessageId()` - 测试单个消息ID撤回
- `testRecallWithProcessFailure()` - 测试撤回失败情况

**覆盖场景**:
- 消息撤回功能
- 单个和多个消息撤回
- 异常情况处理

### 4. austin-handler 模块

#### 4.1 HandlerHolderTest
**位置**: `austin-handler/src/test/java/com/java3y/austin/handler/handler/HandlerHolderTest.java`

**测试内容**:
- `testPutAndRouteHandler()` - 测试注册和路由handler
- `testRouteNonExistentHandler()` - 测试路由不存在的handler
- `testOverwriteHandler()` - 测试覆盖handler
- `testRouteEmptyHolder()` - 测试空holder路由

**覆盖场景**:
- Handler注册和路由机制
- 不存在的handler处理
- Handler覆盖场景

## 测试框架和工具

- **测试框架**: JUnit 5 (JUnit Jupiter)
- **Mock框架**: Mockito
- **断言库**: JUnit 5 Assertions

## 运行测试

### 运行所有测试
```bash
mvn test
```

### 运行特定模块的测试
```bash
# austin-common模块
mvn test -pl austin-common

# austin-support模块
mvn test -pl austin-support

# austin-service-api-impl模块
mvn test -pl austin-service-api-impl

# austin-handler模块
mvn test -pl austin-handler
```

### 运行特定测试类
```bash
# 运行EnumUtilTest
mvn test -Dtest=EnumUtilTest -pl austin-common

# 运行SendServiceImplCompleteTest
mvn test -Dtest=SendServiceImplCompleteTest -pl austin-service-api-impl
```

## 测试覆盖率

建议使用 JaCoCo 生成测试覆盖率报告：

```bash
mvn clean test jacoco:report
```

覆盖率报告将生成在各模块的 `target/site/jacoco/index.html`

## 最佳实践

1. **命名规范**: 测试类以 `Test` 结尾，测试方法以 `test` 开头
2. **AAA模式**: 所有测试遵循 Arrange-Act-Assert 模式
3. **独立性**: 每个测试方法独立，不依赖其他测试
4. **Mock使用**: 使用Mockito模拟外部依赖
5. **异常测试**: 使用 `assertThrows` 测试异常场景
6. **边界测试**: 包含边界值和异常值测试

## 待完善的测试

以下模块可以继续添加更多测试：

1. **austin-cron** - 定时任务模块
2. **austin-data-house** - 数据仓库模块
3. **austin-stream** - 流处理模块
4. **austin-web** - Web接口模块
5. **更多Handler实现** - 各种渠道的具体实现类
6. **去重逻辑** - DeduplicationService相关测试
7. **流控逻辑** - FlowControl相关测试

## 贡献指南

添加新测试时请遵循以下原则：

1. 确保测试有意义且能捕获真实的bug
2. 保持测试简单和可读
3. 为复杂逻辑添加注释说明
4. 测试应该快速执行
5. 避免测试之间的依赖关系

## 注意事项

1. 某些测试依赖Spring Boot容器，需要使用 `@SpringBootTest` 注解
2. 对于涉及数据库操作的测试，建议使用内存数据库(H2)或测试容器(Testcontainers)
3. 对于涉及外部服务调用的测试，务必使用Mock避免真实调用
4. 定期运行测试确保代码质量

## 更新日志

- 2025-12-03: 初始版本，添加核心模块单元测试
  - austin-common: 3个测试类
  - austin-support: 1个测试类
  - austin-service-api-impl: 3个测试类
  - austin-handler: 1个测试类
