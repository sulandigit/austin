package com.java3y.austin.support.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ContentHolderUtil 单元测试
 *
 * @author austin
 */
class ContentHolderUtilTest {

    @Test
    void testReplacePlaceHolderSuccess() {
        // 测试正常替换
        String template = "Hello {$name}, your age is {$age}";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("name", "Austin");
        paramMap.put("age", "25");

        String result = ContentHolderUtil.replacePlaceHolder(template, paramMap);
        assertEquals("Hello Austin, your age is 25", result);
    }

    @Test
    void testReplacePlaceHolderWithMultipleOccurrences() {
        // 测试多次出现同一占位符
        String template = "{$name} says hello to {$name}";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("name", "Austin");

        String result = ContentHolderUtil.replacePlaceHolder(template, paramMap);
        assertEquals("Austin says hello to Austin", result);
    }

    @Test
    void testReplacePlaceHolderWithNoPlaceholder() {
        // 测试没有占位符的情况
        String template = "No placeholder here";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("name", "Austin");

        String result = ContentHolderUtil.replacePlaceHolder(template, paramMap);
        assertEquals("No placeholder here", result);
    }

    @Test
    void testReplacePlaceHolderWithNullParamMap() {
        // 测试paramMap为null
        String template = "Hello {$name}";

        assertThrows(IllegalArgumentException.class, () -> {
            ContentHolderUtil.replacePlaceHolder(template, null);
        });
    }

    @Test
    void testReplacePlaceHolderWithMissingParam() {
        // 测试缺少必需参数
        String template = "Hello {$name}, your age is {$age}";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("name", "Austin");
        // 缺少age参数

        assertThrows(IllegalArgumentException.class, () -> {
            ContentHolderUtil.replacePlaceHolder(template, paramMap);
        });
    }

    @Test
    void testReplacePlaceHolderWithEmptyValue() {
        // 测试空值参数
        String template = "Hello {$name}";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("name", "");

        assertThrows(IllegalArgumentException.class, () -> {
            ContentHolderUtil.replacePlaceHolder(template, paramMap);
        });
    }

    @Test
    void testReplacePlaceHolderWithComplexContent() {
        // 测试复杂内容替换
        String template = "Dear {$userName}, your order {$orderId} has been shipped. Track: {$trackingUrl}";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("userName", "John Doe");
        paramMap.put("orderId", "ORD12345");
        paramMap.put("trackingUrl", "https://tracking.com/ORD12345");

        String result = ContentHolderUtil.replacePlaceHolder(template, paramMap);
        assertEquals("Dear John Doe, your order ORD12345 has been shipped. Track: https://tracking.com/ORD12345", result);
    }

    @Test
    void testReplacePlaceHolderWithSpecialCharacters() {
        // 测试特殊字符
        String template = "Message: {$content}";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("content", "Special chars: @#$%^&*()");

        String result = ContentHolderUtil.replacePlaceHolder(template, paramMap);
        assertEquals("Message: Special chars: @#$%^&*()", result);
    }

    @Test
    void testReplacePlaceHolderWithChineseCharacters() {
        // 测试中文字符
        String template = "你好 {$name}，欢迎使用{$system}";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("name", "张三");
        paramMap.put("system", "Austin消息推送平台");

        String result = ContentHolderUtil.replacePlaceHolder(template, paramMap);
        assertEquals("你好 张三，欢迎使用Austin消息推送平台", result);
    }
}
