# Austin CLI - 命令行工具

## 快速使用

### 基本用法

```bash
# 发送单条消息
java -jar austin-cli.jar \
  -s http://localhost:8080 \
  -t 1 \
  -r "user@example.com" \
  -v '{"code":"123456","userName":"张三"}'

# 批量发送
java -jar austin-cli.jar \
  -s http://localhost:8080 \
  -t 1 \
  -r "user1@example.com,user2@example.com" \
  --batch

# 从文件发送
java -jar austin-cli.jar \
  -s http://localhost:8080 \
  -f config.json
```

## 参数说明

| 参数 | 简写 | 说明 | 必填 |
|------|------|------|------|
| --server | -s | Austin服务地址 | 是 |
| --template | -t | 消息模板ID | 是 |
| --receiver | -r | 接收者 | 是 |
| --variables | -v | 模板变量(JSON) | 否 |
| --bizId | -b | 业务ID | 否 |
| --file | -f | 配置文件 | 否 |
| --code | -c | 业务类型(send/recall) | 否 |
| --batch | | 批量模式 | 否 |
| --timeout | | 超时时间(毫秒) | 否 |
| --retry | | 重试次数 | 否 |
| --verbose | | 详细输出 | 否 |

## 构建

```bash
cd austin-cli
mvn clean package
```
