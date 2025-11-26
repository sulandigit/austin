#!/bin/bash

# 防重放机制验证脚本
# 使用 curl 命令测试防重放功能

BASE_URL="http://localhost:8080"
SECRET="austin-anti-replay-secret-key-2024"

echo "=========================================="
echo "防重放机制验证脚本"
echo "=========================================="

# 生成timestamp和nonce
TIMESTAMP=$(date +%s%3N)
NONCE=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1)

echo ""
echo "步骤1: 准备请求参数"
echo "timestamp: $TIMESTAMP"
echo "nonce: $NONCE"

# 计算签名 (简化版本，实际使用需要完整的HMAC-SHA256)
# 这里需要使用正确的签名算法，可以用Python或Java程序计算
echo ""
echo "步骤2: 计算签名"
echo "注意: 此脚本需要配合实际的签名计算工具使用"
echo ""

# 示例: 正常请求
echo "=========================================="
echo "测试1: 正常请求（需要手动计算签名）"
echo "=========================================="
echo "curl -X POST '$BASE_URL/send' \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -H 'timestamp: $TIMESTAMP' \\"
echo "  -H 'nonce: $NONCE' \\"
echo "  -H 'sign: YOUR_CALCULATED_SIGN' \\"
echo "  -d '{\"code\":\"test_template\"}'"
echo ""

# 示例: 重放攻击
echo "=========================================="
echo "测试2: 重放攻击（使用相同的时间戳和签名）"
echo "=========================================="
echo "执行上面的命令两次，第二次应该被拒绝"
echo ""

# 示例: 过期时间戳
OLD_TIMESTAMP=$((TIMESTAMP - 360000))
echo "=========================================="
echo "测试3: 过期的时间戳"
echo "=========================================="
echo "timestamp: $OLD_TIMESTAMP (6分钟前)"
echo "这个请求应该被拒绝"
echo ""

# 示例: 错误签名
echo "=========================================="
echo "测试4: 错误的签名"
echo "=========================================="
echo "curl -X POST '$BASE_URL/send' \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -H 'timestamp: $TIMESTAMP' \\"
echo "  -H 'nonce: $NONCE' \\"
echo "  -H 'sign: invalid_sign_123' \\"
echo "  -d '{\"code\":\"test_template\"}'"
echo ""

echo "=========================================="
echo "提示:"
echo "1. 请确保服务已启动: $BASE_URL"
echo "2. 使用 SignUtils 工具类生成正确的签名"
echo "3. 查看日志验证防重放机制是否生效"
echo "=========================================="
