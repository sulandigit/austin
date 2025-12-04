# Austin API 版本管理测试指南

## 测试概述

本文档用于验证 Austin 平台 API 版本管理（v1/v2）的实施是否正确。

## 已实施的变更

### 1. SendController（消息发送与撤回）

以下接口现在支持多个路径版本：

- **消息发送**：
  - 历史路径（兼容）：`POST /send`
  - v1 版本：`POST /api/v1/send`
  - v2 版本：`POST /api/v2/send`

- **批量发送**：
  - 历史路径（兼容）：`POST /batchSend`
  - v1 版本：`POST /api/v1/batchSend`
  - v2 版本：`POST /api/v2/batchSend`

- **消息撤回**：
  - 历史路径（兼容）：`POST /recall`
  - v1 版本：`POST /api/v1/recall`
  - v2 版本：`POST /api/v2/recall`

### 2. DataController（消息追踪）

以下接口现在支持多个路径版本：

- **消息链路追踪**：
  - 历史路径（兼容）：`POST /message` 或 `POST /trace/message`
  - v1 版本：`POST /api/v1/trace/message`
  - v2 版本：`POST /api/v2/trace/message`

- **用户消息追踪**：
  - 历史路径（兼容）：`POST /user` 或 `POST /trace/user`
  - v1 版本：`POST /api/v1/trace/user`
  - v2 版本：`POST /api/v2/trace/user`

- **模板消息追踪**：
  - 历史路径（兼容）：`POST /messageTemplate` 或 `POST /trace/messageTemplate`
  - v1 版本：`POST /api/v1/trace/messageTemplate`
  - v2 版本：`POST /api/v2/trace/messageTemplate`

- **短信消息追踪**：
  - 历史路径（兼容）：`POST /sms` 或 `POST /trace/sms`
  - v1 版本：`POST /api/v1/trace/sms`
  - v2 版本：`POST /api/v2/trace/sms`

### 3. Swagger 文档分组

现在有三个独立的 Swagger 文档分组：

1. **用户端接口文档（历史版本）**：展示所有无版本前缀的历史路径
2. **用户端接口文档-v1**：展示所有 `/api/v1/**` 路径
3. **用户端接口文档-v2**：展示所有 `/api/v2/**` 路径（推荐使用）

## 测试方法

### 1. 启动应用

```bash
cd /data/workspace/austin/austin-web
# 根据项目配置启动应用
```

### 2. 访问 Swagger 文档

打开浏览器访问：`http://localhost:8080/swagger-ui/index.html`

在右上角的下拉菜单中，应该能看到三个文档分组：
- 用户端接口文档（历史版本）
- 用户端接口文档-v1
- 用户端接口文档-v2

### 3. 测试接口可达性

#### 测试消息发送接口

```bash
# 测试历史路径（向后兼容）
curl -X POST http://localhost:8080/send \
  -H "Content-Type: application/json" \
  -d '{
    "code": "send",
    "messageTemplateId": 1,
    "messageParam": {
      "receiver": "test@example.com"
    }
  }'

# 测试 v1 路径
curl -X POST http://localhost:8080/api/v1/send \
  -H "Content-Type: application/json" \
  -d '{
    "code": "send",
    "messageTemplateId": 1,
    "messageParam": {
      "receiver": "test@example.com"
    }
  }'

# 测试 v2 路径
curl -X POST http://localhost:8080/api/v2/send \
  -H "Content-Type: application/json" \
  -d '{
    "code": "send",
    "messageTemplateId": 1,
    "messageParam": {
      "receiver": "test@example.com"
    }
  }'
```

#### 测试消息追踪接口

```bash
# 测试历史路径
curl -X POST http://localhost:8080/trace/message \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": "test-message-id"
  }'

# 测试 v1 路径
curl -X POST http://localhost:8080/api/v1/trace/message \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": "test-message-id"
  }'

# 测试 v2 路径
curl -X POST http://localhost:8080/api/v2/trace/message \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": "test-message-id"
  }'
```

## 验收标准

### 功能性验收

- [ ] 所有历史路径（无版本前缀）仍然可用，行为与之前完全一致
- [ ] 所有 `/api/v1/**` 路径可用，行为与历史路径一致
- [ ] 所有 `/api/v2/**` 路径可用，行为与 v1 一致
- [ ] 相同接口的不同版本路径返回的结果一致

### 文档性验收

- [ ] Swagger UI 中能看到三个独立的文档分组
- [ ] "用户端接口文档（历史版本）"组只显示无版本前缀的路径
- [ ] "用户端接口文档-v1"组只显示 `/api/v1/**` 路径
- [ ] "用户端接口文档-v2"组只显示 `/api/v2/**` 路径
- [ ] v1 文档中标注"稳定版本，建议迁移到 v2"
- [ ] v2 文档中标注"推荐使用"

### 兼容性验收

- [ ] 现有客户端调用无版本前缀的接口不受影响
- [ ] 无需修改现有客户端代码即可继续使用

## 注意事项

1. **向后兼容**：所有修改都保持了向后兼容性，现有客户端无需修改
2. **版本行为一致**：当前阶段 v1 和 v2 的行为完全一致，共用同一套业务逻辑
3. **未来演进**：v2 预留了改进空间，未来可以在 v2 中引入新特性而不影响 v1
4. **文档标记**：文档中已明确标记建议使用 v2 版本

## 后续工作建议

1. 在项目 README 或 API 文档中更新版本说明
2. 通知现有客户端开发者，建议逐步迁移到 v2 接口
3. 建立版本生命周期管理流程
4. 设置监控指标，追踪各版本的使用情况
