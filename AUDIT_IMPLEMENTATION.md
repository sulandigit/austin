# 消息发送审批流程实现说明

## 概述
本次实现了完整的消息发送审批流程，包括审批记录管理、审批状态校验、审批历史追溯等核心功能。

## 实现内容

### 1. 数据库变更
在 `doc/sql/austin.sql` 中新增审批记录表：
```sql
CREATE TABLE IF NOT EXISTS `audit_record`
(
    `id`                BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '审批记录ID',
    `template_id`       BIGINT(20)   NOT NULL DEFAULT '0' COMMENT '关联的消息模板ID',
    `audit_status`      TINYINT(4)   NOT NULL DEFAULT '0' COMMENT '审批状态: 10.待审核 20.审核成功 30.被拒绝',
    `auditor`           VARCHAR(45)  NOT NULL DEFAULT '' COMMENT '审批人员',
    `audit_opinion`     VARCHAR(500) NOT NULL DEFAULT '' COMMENT '审批意见',
    `audit_time`        INT(11)      NOT NULL DEFAULT '0' COMMENT '审批时间(秒级时间戳)',
    `template_snapshot` TEXT COMMENT '模板快照(JSON格式,记录审批时的模板配置)',
    `created`           INT(11)      NOT NULL DEFAULT '0' COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_template_created` (`template_id`, `created`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='审批记录表';
```

**执行方式：**
```bash
mysql -u root -p austin < doc/sql/austin.sql
```

### 2. 新增文件列表

#### Domain层
- `austin-support/src/main/java/com/java3y/austin/support/domain/AuditRecord.java` - 审批记录实体类

#### DAO层
- `austin-support/src/main/java/com/java3y/austin/support/dao/AuditRecordDao.java` - 审批记录DAO接口

#### VO层
- `austin-web/src/main/java/com/java3y/austin/web/vo/AuditTemplateParam.java` - 审批操作请求参数
- `austin-web/src/main/java/com/java3y/austin/web/vo/BatchAuditParam.java` - 批量审批请求参数
- `austin-web/src/main/java/com/java3y/austin/web/vo/AuditRecordVo.java` - 审批记录返回VO
- `austin-web/src/main/java/com/java3y/austin/web/vo/BatchAuditResultVo.java` - 批量审批结果VO

#### Service层
- `austin-web/src/main/java/com/java3y/austin/web/service/AuditService.java` - 审批服务接口
- `austin-web/src/main/java/com/java3y/austin/web/service/impl/AuditServiceImpl.java` - 审批服务实现

#### Controller层
- `austin-web/src/main/java/com/java3y/austin/web/controller/AuditController.java` - 审批管理Controller

### 3. 修改文件
- `austin-web/src/main/java/com/java3y/austin/web/service/impl/MessageTemplateServiceImpl.java`
  - 在 `startCronTask` 方法中增加审批状态校验，只有审核成功的模板才能启动定时任务

## API接口说明

### 1. 审批模板
**接口地址：** `POST /audit/approve`

**请求参数：**
```json
{
  "templateId": 1,
  "auditStatus": 20,
  "auditOpinion": "审批通过",
  "auditor": "admin"
}
```

**参数说明：**
- templateId: 模板ID（必填）
- auditStatus: 审批状态，20-通过，30-拒绝（必填）
- auditOpinion: 审批意见，最多500字符（非必填）
- auditor: 审批人（必填）

**返回示例：**
```json
{
  "status": "0",
  "msg": "操作成功"
}
```

### 2. 批量审批模板
**接口地址：** `POST /audit/batchApprove`

**请求参数：**
```json
{
  "templateIds": [1, 2, 3],
  "auditStatus": 20,
  "auditOpinion": "批量审批通过",
  "auditor": "admin"
}
```

**返回示例：**
```json
{
  "successCount": 2,
  "failCount": 1,
  "failDetails": [
    {
      "templateId": 3,
      "reason": "当前模板状态不允许审批"
    }
  ]
}
```

### 3. 查询待审批列表
**接口地址：** `GET /audit/pending`

**请求参数：**
- pageNum: 页码，默认1
- pageSize: 每页数量，默认10
- sendChannel: 发送渠道（可选）
- creator: 创建者（可选）

**返回示例：**
```json
{
  "count": 5,
  "rows": [
    {
      "id": 1,
      "name": "测试模板",
      "auditStatus": 10,
      "creator": "user1",
      "created": 1699999999
    }
  ]
}
```

### 4. 查询审批历史
**接口地址：** `GET /audit/history/{templateId}`

**返回示例：**
```json
[
  {
    "id": 1,
    "templateId": 1,
    "auditStatus": 20,
    "auditStatusDesc": "审核成功",
    "auditor": "admin",
    "auditOpinion": "审批通过",
    "auditTime": 1699999999,
    "templateSnapshot": "{...}",
    "created": 1699999999
  }
]
```

## 业务流程说明

### 1. 模板创建流程
1. 用户创建消息模板
2. 系统自动设置审批状态为"待审核"（状态码：10）
3. 模板进入待审批列表

### 2. 模板修改流程
1. 用户修改已审核通过的模板
2. 系统检测到关键字段变更
3. 自动重置审批状态为"待审核"
4. 如果有关联的定时任务，自动停止

### 3. 审批流程
1. 管理员查看待审批列表
2. 选择模板进行审批
3. 填写审批意见（可选）
4. 提交审批决策（通过/拒绝）
5. 系统更新模板审批状态
6. 创建审批历史记录，保存模板快照

### 4. 启动定时任务流程
1. 用户尝试启动定时任务
2. 系统校验模板审批状态
3. 只有"审核成功"（状态码：20）的模板才能启动
4. 其他状态返回错误提示

## 审批状态说明

| 状态码 | 状态名称 | 说明 |
|--------|---------|------|
| 10 | 待审核 | 模板创建或修改后的初始状态 |
| 20 | 审核成功 | 管理员审批通过，可用于生产环境 |
| 30 | 被拒绝 | 审批被拒绝，需要修改后重新提交 |

## 关键特性

### 1. 事务保障
- 审批操作（更新模板状态 + 创建审批记录）在同一事务中执行
- 保证数据一致性

### 2. 审批历史追溯
- 每次审批都会创建历史记录
- 保存审批时的模板快照（JSON格式）
- 支持按时间倒序查询审批历史

### 3. 模板快照
- 记录审批时的完整模板配置
- 用于审计和追溯
- JSON格式存储，方便解析

### 4. 批量操作
- 支持批量审批多个模板
- 返回成功和失败的详细信息
- 失败不影响其他模板的审批

## 使用示例

### 场景1：审批通过模板
```bash
curl -X POST http://localhost:8080/audit/approve \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": 1,
    "auditStatus": 20,
    "auditOpinion": "模板配置正确，审批通过",
    "auditor": "admin"
  }'
```

### 场景2：批量审批
```bash
curl -X POST http://localhost:8080/audit/batchApprove \
  -H "Content-Type: application/json" \
  -d '{
    "templateIds": [1, 2, 3],
    "auditStatus": 20,
    "auditOpinion": "批量审批通过",
    "auditor": "admin"
  }'
```

### 场景3：查询待审批列表
```bash
curl -X GET "http://localhost:8080/audit/pending?pageNum=1&pageSize=10"
```

### 场景4：查询审批历史
```bash
curl -X GET "http://localhost:8080/audit/history/1"
```

## 注意事项

1. **审批状态校验**
   - 只有状态为"待审核"的模板才能进行审批操作
   - 已审批的模板需要重新提交才能再次审批

2. **定时任务限制**
   - 只有审核成功的模板才能启动定时任务
   - 模板修改后会自动停止定时任务

3. **审批意见长度**
   - 审批意见最多500字符
   - 超出长度会返回参数错误

4. **并发控制**
   - 审批操作使用事务保障
   - 避免并发审批导致的数据不一致

## 扩展点

### 1. 多级审批
可以在审批记录表中增加审批级别字段，支持多级审批流程。

### 2. 审批通知
可以集成消息通知机制，在审批后通知模板创建者。

### 3. 权限控制
可以集成统一权限管理系统，实现更细粒度的审批权限控制。

### 4. 工单系统对接
可以利用 `message_template` 表的 `flow_id` 字段对接外部工单系统。

## 测试建议

1. **单元测试**
   - 测试审批服务的各个方法
   - 验证事务回滚机制
   - 测试边界条件

2. **集成测试**
   - 测试完整的审批流程
   - 验证审批状态流转
   - 测试定时任务启动限制

3. **性能测试**
   - 测试批量审批性能
   - 验证大量审批记录的查询性能
   - 测试并发审批场景

## 总结

本次实现完成了消息发送审批流程的核心功能，包括：
- ✅ 审批记录表设计和创建
- ✅ 审批实体类和DAO层实现
- ✅ 审批服务接口和业务逻辑
- ✅ 审批管理Controller和API接口
- ✅ 模板服务增强，集成审批状态校验
- ✅ 完整的审批历史追溯能力
- ✅ 批量审批支持

所有代码已通过编译检查，无语法错误，可以直接使用。
