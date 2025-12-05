#!/bin/bash

# 线程池动态调整功能测试脚本
# 使用方法: bash test_thread_pool_api.sh

# 服务地址
BASE_URL="http://localhost:8080"

echo "======================================"
echo "线程池动态调整功能测试"
echo "======================================"
echo ""

# 测试1: 查询所有线程池信息
echo "测试1: 查询所有线程池信息"
echo "执行命令: GET ${BASE_URL}/threadPool/list"
curl -s -X GET "${BASE_URL}/threadPool/list" | python3 -m json.tool
echo ""
echo "--------------------------------------"
echo ""

# 测试2: 查询指定线程池信息
POOL_NAME="execute-xxl-thread-pool"
echo "测试2: 查询指定线程池信息 (${POOL_NAME})"
echo "执行命令: GET ${BASE_URL}/threadPool/get/${POOL_NAME}"
curl -s -X GET "${BASE_URL}/threadPool/get/${POOL_NAME}" | python3 -m json.tool
echo ""
echo "--------------------------------------"
echo ""

# 测试3: 调整线程池参数
echo "测试3: 调整线程池参数 (${POOL_NAME})"
echo "调整参数: corePoolSize=5, maximumPoolSize=10, keepAliveTime=60"
curl -s -X POST "${BASE_URL}/threadPool/adjust" \
  -H "Content-Type: application/json" \
  -d '{
    "threadPoolName": "execute-xxl-thread-pool",
    "corePoolSize": 5,
    "maximumPoolSize": 10,
    "keepAliveTime": 60
  }' | python3 -m json.tool
echo ""
echo "--------------------------------------"
echo ""

# 测试4: 验证调整后的参数
echo "测试4: 验证调整后的参数"
echo "执行命令: GET ${BASE_URL}/threadPool/get/${POOL_NAME}"
curl -s -X GET "${BASE_URL}/threadPool/get/${POOL_NAME}" | python3 -m json.tool
echo ""
echo "--------------------------------------"
echo ""

# 测试5: 批量调整线程池参数
echo "测试5: 批量调整线程池参数"
curl -s -X POST "${BASE_URL}/threadPool/batchAdjust" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "threadPoolName": "execute-xxl-thread-pool",
      "corePoolSize": 3,
      "maximumPoolSize": 3,
      "keepAliveTime": 50
    }
  ]' | python3 -m json.tool
echo ""
echo "--------------------------------------"
echo ""

# 测试6: 参数校验测试(核心线程数大于最大线程数)
echo "测试6: 参数校验测试 - 核心线程数大于最大线程数(应该失败)"
curl -s -X POST "${BASE_URL}/threadPool/adjust" \
  -H "Content-Type: application/json" \
  -d '{
    "threadPoolName": "execute-xxl-thread-pool",
    "corePoolSize": 10,
    "maximumPoolSize": 5,
    "keepAliveTime": 50
  }' | python3 -m json.tool
echo ""
echo "--------------------------------------"
echo ""

echo "======================================"
echo "测试完成"
echo "======================================"
