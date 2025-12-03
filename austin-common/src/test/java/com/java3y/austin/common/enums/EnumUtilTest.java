package com.java3y.austin.common.enums;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnumUtil 工具类单元测试
 *
 * @author austin
 */
class EnumUtilTest {

    @Test
    void testGetDescriptionByCode() {
        // 测试获取描述
        String description = EnumUtil.getDescriptionByCode(10, IdType.class);
        assertEquals("userId", description);

        description = EnumUtil.getDescriptionByCode(30, IdType.class);
        assertEquals("phone", description);

        // 测试不存在的code
        description = EnumUtil.getDescriptionByCode(999, IdType.class);
        assertEquals("", description);
    }

    @Test
    void testGetEnumByCode() {
        // 测试获取枚举
        IdType idType = EnumUtil.getEnumByCode(10, IdType.class);
        assertNotNull(idType);
        assertEquals(IdType.USER_ID, idType);

        idType = EnumUtil.getEnumByCode(50, IdType.class);
        assertNotNull(idType);
        assertEquals(IdType.EMAIL, idType);

        // 测试不存在的code
        idType = EnumUtil.getEnumByCode(999, IdType.class);
        assertNull(idType);
    }

    @Test
    void testGetCodeList() {
        // 测试获取code列表
        List<Integer> codeList = EnumUtil.getCodeList(SmsStatus.class);
        assertNotNull(codeList);
        assertEquals(4, codeList.size());
        assertTrue(codeList.contains(10));
        assertTrue(codeList.contains(20));
        assertTrue(codeList.contains(30));
        assertTrue(codeList.contains(40));
    }

    @Test
    void testGetCodeListForDeduplicationType() {
        List<Integer> codeList = EnumUtil.getCodeList(DeduplicationType.class);
        assertNotNull(codeList);
        assertEquals(2, codeList.size());
        assertTrue(codeList.contains(10));
        assertTrue(codeList.contains(20));
    }
}
