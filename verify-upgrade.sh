#!/bin/bash

# Austin 项目 Java 21 升级验证脚本

echo "========================================="
echo "Austin 项目 Java 21 升级验证"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 验证计数器
PASS=0
FAIL=0

# 验证函数
verify() {
    local test_name=$1
    local command=$2
    
    echo -n "验证: $test_name ... "
    
    if eval "$command" > /dev/null 2>&1; then
        echo -e "${GREEN}通过${NC}"
        ((PASS++))
        return 0
    else
        echo -e "${RED}失败${NC}"
        ((FAIL++))
        return 1
    fi
}

# 验证 grep 函数
verify_grep() {
    local test_name=$1
    local pattern=$2
    local expected=$3
    
    echo -n "验证: $test_name ... "
    
    local count=$(find . -name "*.java" -type f -exec grep -l "$pattern" {} \; 2>/dev/null | wc -l)
    
    if [ "$count" -eq "$expected" ]; then
        echo -e "${GREEN}通过${NC} (发现 $count 个文件)"
        ((PASS++))
        return 0
    else
        echo -e "${RED}失败${NC} (期望 $expected 个文件, 实际 $count 个)"
        ((FAIL++))
        return 1
    fi
}

echo "1. POM 文件验证"
echo "-------------------"

# 验证主 POM 中的 Java 版本
verify "主 POM Java 版本为 21" \
    "grep -q '<java.version>21</java.version>' pom.xml"

# 验证 Spring Boot 版本
verify "Spring Boot 版本为 3.2.0" \
    "grep -q '<version>3.2.0</version>' pom.xml | head -1"

# 验证 MySQL 驱动版本
verify "MySQL 驱动版本为 8.0.33" \
    "grep -q '<mysql-connector-java.version>8.0.33</mysql-connector-java.version>' pom.xml"

# 验证 FastJSON 版本
verify "FastJSON 版本为 2.0.43" \
    "grep -q '<fastjson.version>2.0.43</fastjson.version>' pom.xml"

# 验证 SpringDoc 依赖
verify "使用 SpringDoc 替代 Swagger" \
    "grep -q 'springdoc-openapi-starter-webmvc-ui' pom.xml"

echo ""
echo "2. Java 代码验证"
echo "-------------------"

# 验证没有遗留的 javax 包
verify_grep "无遗留的 javax.annotation 导入" "import javax\.annotation\." 0
verify_grep "无遗留的 javax.persistence 导入" "import javax\.persistence\." 0
verify_grep "无遗留的 javax.servlet 导入" "import javax\.servlet\." 0
verify_grep "无遗留的 javax.validation 导入" "import javax\.validation\." 0

# 验证 FastJSON 包名更新
verify_grep "无遗留的旧版 FastJSON 导入" "import com\.alibaba\.fastjson\." 0

# 验证 Swagger 注解迁移
verify_grep "无遗留的旧版 Swagger 导入" "import io\.swagger\.annotations\." 0
verify_grep "无遗留的 Springfox 导入" "import springfox\." 0

echo ""
echo "3. 配置文件验证"
echo "-------------------"

# 验证 MySQL 驱动类名
verify "MySQL 驱动类名已更新" \
    "grep -q 'com.mysql.cj.jdbc.Driver' austin-web/src/main/resources/application.properties"

echo ""
echo "4. Docker 配置验证"
echo "-------------------"

# 验证 Dockerfile
verify "Dockerfile 使用 OpenJDK 21" \
    "grep -q 'openjdk:21' Dockerfile"

echo ""
echo "5. 文档验证"
echo "-------------------"

# 验证 README 更新
verify "README 显示 JDK 21" \
    "grep -q 'JDK-21' README.md"

verify "README 显示 Spring Boot 3.2.0" \
    "grep -q 'SpringBoot-3.2.0' README.md"

verify "README 显示 MySQL 8.0.x" \
    "grep -q 'MySQL-8.0.x' README.md"

# 验证升级文档存在
verify "升级文档已创建" \
    "test -f UPGRADE_TO_JAVA21.md"

echo ""
echo "========================================="
echo "验证总结"
echo "========================================="
echo -e "通过: ${GREEN}$PASS${NC}"
echo -e "失败: ${RED}$FAIL${NC}"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✓ 所有验证都已通过!${NC}"
    echo ""
    echo "下一步:"
    echo "1. 安装 Java 21 和 Maven 3.6+"
    echo "2. 运行: mvn clean compile"
    echo "3. 运行: mvn test"
    echo "4. 启动应用并测试功能"
    exit 0
else
    echo -e "${RED}✗ 部分验证失败,请检查上述失败项${NC}"
    exit 1
fi
