# API签名验证使用指南

## 一、功能说明

API签名验证功能为Austin消息推送平台的对外API提供安全保护，通过HMAC-SHA256签名算法验证请求的完整性和来源合法性，防止参数篡改和重放攻击。

## 二、涉及的API接口

签名验证目前覆盖以下核心API：
- `/send` - 单个文案发送接口
- `/batchSend` - 批量发送接口  
- `/recall` - 消息撤回接口

## 三、配置说明

### 1. 全局配置

在 `application.properties` 中配置：

```properties
# 签名验证全局开关：true-启用，false-禁用（默认禁用）
austin.signature.enabled=false

# 签名验证模式：strict-严格模式（验证失败拒绝请求），log-日志模式（仅记录日志不拦截）
austin.signature.mode=log
```

### 2. 应用级配置

在数据库中创建应用信息，每个调用方对应一个应用记录：

```sql
-- 插入测试应用
INSERT INTO `app_info` (
    `app_id`, `app_name`, `app_secret`, `status`, 
    `sign_version`, `signature_enabled`, `remark`, 
    `created`, `updated`, `is_deleted`
) VALUES (
    'test-app-001', 
    '测试应用', 
    'test-secret-key-123456',
    1,  -- 状态：1-启用
    'v1',  -- 签名版本
    1,  -- 是否强制启用签名验证：1-启用
    '这是一个测试应用',
    UNIX_TIMESTAMP(),
    UNIX_TIMESTAMP(),
    0
);
```

## 四、签名算法说明

### 1. 签名参数

客户端需要在HTTP请求头中携带以下参数：

| 请求头字段 | 说明 | 示例 |
|----------|------|------|
| X-Austin-App-Id | 应用ID | test-app-001 |
| X-Austin-Timestamp | 请求时间戳（秒） | 1701676800 |
| X-Austin-Nonce | 随机字符串（16-32位字母数字） | abcd1234efgh5678 |
| X-Austin-Signature | 请求签名 | a1b2c3... |
| X-Austin-Sign-Version | 签名版本（可选，默认v1） | v1 |

### 2. 签名生成步骤

**第一步：构造待签名字符串**

按照以下顺序拼接字段，字段间使用换行符(`\n`)分隔：

```
appId + "\n" +
timestamp + "\n" +
nonce + "\n" +
HTTP方法（大写） + "\n" +
请求路径 + "\n" +
请求体SHA256摘要
```

示例：
```
test-app-001
1701676800
abcd1234efgh5678
POST
/send
a1b2c3d4e5f6...（请求体的SHA256摘要）
```

**第二步：使用HMAC-SHA256计算签名**

使用应用密钥（appSecret）对待签名字符串进行HMAC-SHA256计算，并转为十六进制字符串。

```java
// Java示例代码
Mac mac = Mac.getInstance("HmacSHA256");
SecretKeySpec secretKeySpec = new SecretKeySpec(
    appSecret.getBytes(StandardCharsets.UTF_8), 
    "HmacSHA256"
);
mac.init(secretKeySpec);
byte[] signBytes = mac.doFinal(signString.getBytes(StandardCharsets.UTF_8));
String signature = bytesToHex(signBytes);
```

### 3. 请求体处理规则

- 对于POST/PUT等有请求体的请求，需要对请求体JSON进行规范化处理
- 移除所有多余空白符（换行、缩进等）
- 对规范化后的JSON字符串计算SHA256摘要
- 如果请求体为空，则使用空字符串

## 五、调用示例

### Java调用示例

```java
import com.java3y.austin.support.utils.SignatureUtils;

// 1. 准备请求参数
String appId = "test-app-001";
String appSecret = "test-secret-key-123456";
String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
String method = "POST";
String path = "/send";
String body = "{\"code\":\"send\",\"messageTemplateId\":1,\"messageParam\":{\"receiver\":\"test@example.com\"}}";

// 2. 生成签名
String signature = SignatureUtils.generateSignature(
    appId, timestamp, nonce, method, path, body, appSecret, "v1"
);

// 3. 发送HTTP请求
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/send"))
    .header("Content-Type", "application/json")
    .header("X-Austin-App-Id", appId)
    .header("X-Austin-Timestamp", timestamp)
    .header("X-Austin-Nonce", nonce)
    .header("X-Austin-Signature", signature)
    .header("X-Austin-Sign-Version", "v1")
    .POST(HttpRequest.BodyPublishers.ofString(body))
    .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println(response.body());
```

### cURL调用示例

```bash
#!/bin/bash

APP_ID="test-app-001"
APP_SECRET="test-secret-key-123456"
TIMESTAMP=$(date +%s)
NONCE=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 16 | head -n 1)
METHOD="POST"
PATH="/send"
BODY='{"code":"send","messageTemplateId":1}'

# 注意：这里需要使用SignatureUtils生成签名，cURL无法直接计算
# 实际使用时需要调用Java程序或编写脚本来生成签名

curl -X POST http://localhost:8080/send \
  -H "Content-Type: application/json" \
  -H "X-Austin-App-Id: $APP_ID" \
  -H "X-Austin-Timestamp: $TIMESTAMP" \
  -H "X-Austin-Nonce: $NONCE" \
  -H "X-Austin-Signature: [生成的签名]" \
  -H "X-Austin-Sign-Version: v1" \
  -d "$BODY"
```

## 六、错误码说明

| 错误码 | 说明 | 解决方法 |
|-------|------|---------|
| S0001 | 签名参数缺失 | 检查是否传入了appId、timestamp、nonce、signature |
| S0002 | 签名参数格式错误 | 检查nonce长度是否在16-32位，timestamp是否为数字 |
| S0003 | 应用不存在或已被禁用 | 检查appId是否正确，应用是否已启用 |
| S0004 | 请求已超时 | 检查客户端时间是否准确，请求是否在5分钟内 |
| S0005 | 检测到重放攻击 | 每次请求使用新的nonce，避免重复 |
| S0006 | 签名验证失败 | 检查签名算法是否正确，appSecret是否正确 |
| S0007 | 签名版本不支持 | 检查signVersion是否与应用配置一致 |

## 七、灰度策略

### 阶段1：日志模式（推荐）

首次上线时，建议使用日志模式：

```properties
austin.signature.enabled=true
austin.signature.mode=log
```

此模式下，签名验证失败只记录日志，不拦截请求，用于观察调用方适配情况。

### 阶段2：应用级强制

对特定应用启用强制验证：

```sql
-- 为特定应用启用强制验证
UPDATE app_info SET signature_enabled = 1 WHERE app_id = 'target-app-id';
```

此时即使全局为log模式，该应用也会被强制验证。

### 阶段3：严格模式

确认所有调用方适配完成后，切换到严格模式：

```properties
austin.signature.enabled=true
austin.signature.mode=strict
```

此模式下，签名验证失败的请求将被拒绝。

## 八、安全建议

1. **使用HTTPS**：签名机制防止参数篡改，但仍需HTTPS保证传输层安全
2. **密钥保护**：appSecret应安全存储，避免硬编码或泄露
3. **时钟同步**：客户端服务器时间应准确，误差不超过5分钟
4. **nonce唯一性**：每次请求使用新的随机nonce
5. **定期轮换密钥**：建议定期更换appSecret
6. **监控告警**：关注签名失败日志，及时发现异常访问

## 九、常见问题

### Q1: 签名验证失败，但确认签名算法正确？

**A:** 检查以下几点：
- 请求体JSON是否完全一致（空格、换行等）
- timestamp是否在有效期内
- nonce是否重复使用
- HTTP方法是否大写

### Q2: 如何在不影响现有业务的情况下接入？

**A:** 采用灰度策略：
1. 先使用log模式上线，观察日志
2. 针对测试应用启用强制验证
3. 确认无问题后，逐步切换到strict模式

### Q3: 如何支持新的签名算法？

**A:** 在SignatureUtils中添加新版本处理逻辑，并更新应用的signVersion配置。

## 十、测试数据

```sql
-- 插入测试应用数据
INSERT INTO `app_info` VALUES 
('test-app-001', '测试应用1', 'test-secret-key-123456', 1, 'v1', NULL, 1, '测试用应用', UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 0),
('demo-app-002', '演示应用2', 'demo-secret-key-abcdef', 1, 'v1', NULL, 0, '演示用应用（不强制验证）', UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 0);
```
