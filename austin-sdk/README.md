# Austin SDK 使用指南

## 简介

Austin SDK 是消息推送平台 Austin 的 Java 客户端开发工具包，提供了简单易用的 API 来集成 Austin 消息推送服务。

## 特性

- ✅ 简单易用的 API
- ✅ 支持单条和批量消息发送
- ✅ 支持消息撤回
- ✅ 自动重试机制
- ✅ 完善的异常处理
- ✅ 支持日志记录
- ✅ 支持自定义超时配置
- ✅ 支持鉴权机制

## 快速开始

### 1. 添加依赖

在您的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.java3y.austin</groupId>
    <artifactId>austin-sdk</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 创建客户端

```java
// 创建配置
AustinConfig config = AustinConfig.builder()
    .serverUrl("http://localhost:8080")  // Austin服务地址
    .connectTimeout(10000)               // 连接超时10秒
    .readTimeout(30000)                  // 读取超时30秒
    .enableLog(true)                     // 启用日志
    .maxRetryCount(3)                    // 最大重试3次
    .build();

// 创建客户端
AustinClient client = new AustinClient(config);
```

### 3. 发送消息

#### 发送单条消息

```java
// 构建消息参数
MessageParam messageParam = MessageParam.builder()
    .receiver("user@example.com")      // 接收者
    .build();

// 构建发送请求
SendRequest sendRequest = SendRequest.builder()
    .code("send")                      // 业务类型：send
    .messageTemplateId(1L)             // 消息模板ID
    .messageParam(messageParam)
    .build();

// 发送消息
SendResponse response = client.send(sendRequest);
```

#### 发送带变量的消息

```java
// 构建变量（用于模板占位符替换）
Map<String, String> variables = new HashMap<>();
variables.put("userName", "张三");
variables.put("code", "123456");

MessageParam messageParam = MessageParam.builder()
    .bizId("order_001")                // 业务ID（可选）
    .receiver("13800138000")           // 接收者
    .variables(variables)              // 模板变量
    .build();

SendRequest sendRequest = SendRequest.builder()
    .code("send")
    .messageTemplateId(2L)
    .messageParam(messageParam)
    .build();

SendResponse response = client.send(sendRequest);
```

#### 批量发送消息

```java
List<MessageParam> messageParamList = new ArrayList<>();

// 添加多个接收者
messageParamList.add(MessageParam.builder()
    .receiver("user1@example.com")
    .variables(variables1)
    .build());

messageParamList.add(MessageParam.builder()
    .receiver("user2@example.com")
    .variables(variables2)
    .build());

BatchSendRequest batchRequest = BatchSendRequest.builder()
    .code("send")
    .messageTemplateId(1L)
    .messageParamList(messageParamList)
    .build();

SendResponse response = client.batchSend(batchRequest);
```

### 4. 撤回消息

```java
List<String> messageIds = Arrays.asList("message-id-1", "message-id-2");

SendRequest recallRequest = SendRequest.builder()
    .code("recall")
    .messageTemplateId(1L)
    .recallMessageIds(messageIds)
    .build();

SendResponse response = client.recall(recallRequest);
```

### 5. 关闭客户端

```java
client.shutdown();
```

## 配置说明

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| serverUrl | String | 是 | - | Austin服务地址 |
| connectTimeout | Integer | 否 | 10000 | 连接超时时间（毫秒） |
| readTimeout | Integer | 否 | 30000 | 读取超时时间（毫秒） |
| writeTimeout | Integer | 否 | 30000 | 写入超时时间（毫秒） |
| enableLog | Boolean | 否 | true | 是否启用日志 |
| maxRetryCount | Integer | 否 | 3 | 最大重试次数 |
| appKey | String | 否 | - | 应用Key（用于鉴权） |
| appSecret | String | 否 | - | 应用密钥（用于鉴权） |

## 异常处理

SDK 使用 `AustinSdkException` 统一处理异常：

```java
try {
    SendResponse response = client.send(sendRequest);
} catch (AustinSdkException e) {
    System.err.println("错误码: " + e.getCode());
    System.err.println("错误信息: " + e.getMessage());
}
```

## 最佳实践

1. **单例模式**：建议在应用中使用单例模式创建 `AustinClient`，避免频繁创建和销毁
2. **资源释放**：应用关闭时记得调用 `client.shutdown()` 释放资源
3. **异常处理**：捕获 `AustinSdkException` 并进行适当的错误处理
4. **重试机制**：SDK 内置了重试机制，无需手动重试
5. **日志记录**：生产环境可以关闭详细日志以提高性能

## 支持的消息渠道

- 短信（SMS）
- 邮件（Email）
- 微信服务号（模板消息）
- 微信小程序（订阅消息）
- 钉钉（群机器人、工作消息）
- 企业微信（机器人消息、应用消息）
- 飞书机器人
- 支付宝小程序
- Android Push 通知栏

## 更多示例

请参考 `AustinSdkExample.java` 获取更多使用示例。

## 问题反馈

如有问题，请提交 Issue 或联系技术支持。
