#!/bin/bash

# Austin 单元测试运行脚本
# 用途: 快速运行各模块的单元测试

set -e

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Austin 单元测试运行脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 函数: 显示帮助信息
show_help() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -a, --all              运行所有模块的测试"
    echo "  -c, --common           运行 austin-common 模块测试"
    echo "  -s, --support          运行 austin-support 模块测试"
    echo "  -i, --service-impl     运行 austin-service-api-impl 模块测试"
    echo "  -h, --handler          运行 austin-handler 模块测试"
    echo "  -r, --report           生成测试覆盖率报告"
    echo "  --help                 显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 --all               # 运行所有测试"
    echo "  $0 --common            # 只运行common模块测试"
    echo "  $0 --report            # 生成覆盖率报告"
    exit 0
}

# 函数: 运行指定模块的测试
run_test() {
    local module=$1
    local module_name=$2
    
    echo -e "${YELLOW}正在运行 ${module_name} 模块测试...${NC}"
    
    if mvn test -pl ${module} -q; then
        echo -e "${GREEN}✓ ${module_name} 模块测试通过${NC}"
        return 0
    else
        echo -e "${RED}✗ ${module_name} 模块测试失败${NC}"
        return 1
    fi
}

# 函数: 运行所有测试
run_all_tests() {
    echo -e "${BLUE}开始运行所有模块测试...${NC}"
    echo ""
    
    local failed=0
    
    run_test "austin-common" "austin-common" || ((failed++))
    echo ""
    
    run_test "austin-support" "austin-support" || ((failed++))
    echo ""
    
    run_test "austin-service-api-impl" "austin-service-api-impl" || ((failed++))
    echo ""
    
    run_test "austin-handler" "austin-handler" || ((failed++))
    echo ""
    
    echo -e "${BLUE}========================================${NC}"
    if [ $failed -eq 0 ]; then
        echo -e "${GREEN}所有测试通过! 🎉${NC}"
    else
        echo -e "${RED}有 $failed 个模块测试失败${NC}"
        exit 1
    fi
}

# 函数: 生成测试覆盖率报告
generate_coverage_report() {
    echo -e "${YELLOW}正在生成测试覆盖率报告...${NC}"
    
    if mvn clean test jacoco:report; then
        echo -e "${GREEN}✓ 覆盖率报告生成成功${NC}"
        echo ""
        echo -e "${BLUE}覆盖率报告位置:${NC}"
        echo "  - austin-common: austin-common/target/site/jacoco/index.html"
        echo "  - austin-support: austin-support/target/site/jacoco/index.html"
        echo "  - austin-service-api-impl: austin-service-api-impl/target/site/jacoco/index.html"
        echo "  - austin-handler: austin-handler/target/site/jacoco/index.html"
    else
        echo -e "${RED}✗ 覆盖率报告生成失败${NC}"
        exit 1
    fi
}

# 检查参数
if [ $# -eq 0 ]; then
    echo -e "${YELLOW}未指定选项，显示帮助信息${NC}"
    echo ""
    show_help
fi

# 解析命令行参数
case "$1" in
    -a|--all)
        run_all_tests
        ;;
    -c|--common)
        run_test "austin-common" "austin-common"
        ;;
    -s|--support)
        run_test "austin-support" "austin-support"
        ;;
    -i|--service-impl)
        run_test "austin-service-api-impl" "austin-service-api-impl"
        ;;
    -h|--handler)
        run_test "austin-handler" "austin-handler"
        ;;
    -r|--report)
        generate_coverage_report
        ;;
    --help)
        show_help
        ;;
    *)
        echo -e "${RED}错误: 未知选项 '$1'${NC}"
        echo ""
        show_help
        ;;
esac

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}测试完成!${NC}"
echo -e "${BLUE}========================================${NC}"
