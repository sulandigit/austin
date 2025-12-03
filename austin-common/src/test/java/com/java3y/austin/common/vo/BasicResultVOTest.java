package com.java3y.austin.common.vo;

import com.java3y.austin.common.enums.RespStatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BasicResultVO 单元测试
 *
 * @author austin
 */
class BasicResultVOTest {

    @Test
    void testSuccessWithoutData() {
        // 测试无数据的成功响应
        BasicResultVO<Void> result = BasicResultVO.success();

        assertNotNull(result);
        assertEquals(RespStatusEnum.SUCCESS.getCode(), result.getStatus());
        assertEquals(RespStatusEnum.SUCCESS.getMsg(), result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testSuccessWithMessage() {
        // 测试带自定义消息的成功响应
        String customMsg = "操作成功完成";
        BasicResultVO<Void> result = BasicResultVO.success(customMsg);

        assertNotNull(result);
        assertEquals(RespStatusEnum.SUCCESS.getCode(), result.getStatus());
        assertEquals(customMsg, result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testSuccessWithData() {
        // 测试带数据的成功响应
        String testData = "test data";
        BasicResultVO<String> result = BasicResultVO.success(testData);

        assertNotNull(result);
        assertEquals(RespStatusEnum.SUCCESS.getCode(), result.getStatus());
        assertEquals(RespStatusEnum.SUCCESS.getMsg(), result.getMsg());
        assertEquals(testData, result.getData());
    }

    @Test
    void testFailWithoutParams() {
        // 测试默认失败响应
        BasicResultVO<Void> result = BasicResultVO.fail();

        assertNotNull(result);
        assertEquals(RespStatusEnum.FAIL.getCode(), result.getStatus());
        assertEquals(RespStatusEnum.FAIL.getMsg(), result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testFailWithMessage() {
        // 测试带自定义错误消息的失败响应
        String errorMsg = "自定义错误信息";
        BasicResultVO<Void> result = BasicResultVO.fail(errorMsg);

        assertNotNull(result);
        assertEquals(RespStatusEnum.FAIL.getCode(), result.getStatus());
        assertEquals(errorMsg, result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testFailWithStatus() {
        // 测试带自定义状态的失败响应
        BasicResultVO<Void> result = BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR);

        assertNotNull(result);
        assertEquals(RespStatusEnum.SERVICE_ERROR.getCode(), result.getStatus());
        assertEquals(RespStatusEnum.SERVICE_ERROR.getMsg(), result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testFailWithStatusAndMessage() {
        // 测试带自定义状态和消息的失败响应
        String customMsg = "服务器内部错误";
        BasicResultVO<Void> result = BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, customMsg);

        assertNotNull(result);
        assertEquals(RespStatusEnum.SERVICE_ERROR.getCode(), result.getStatus());
        assertEquals(customMsg, result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testConstructorWithRespStatusEnum() {
        // 测试使用RespStatusEnum构造
        BasicResultVO<String> result = new BasicResultVO<>(RespStatusEnum.SUCCESS, "test data");

        assertNotNull(result);
        assertEquals(RespStatusEnum.SUCCESS.getCode(), result.getStatus());
        assertEquals(RespStatusEnum.SUCCESS.getMsg(), result.getMsg());
        assertEquals("test data", result.getData());
    }

    @Test
    void testConstructorWithStatusMsgData() {
        // 测试使用完整参数构造
        BasicResultVO<Integer> result = new BasicResultVO<>(
                RespStatusEnum.SUCCESS,
                "自定义消息",
                100
        );

        assertNotNull(result);
        assertEquals(RespStatusEnum.SUCCESS.getCode(), result.getStatus());
        assertEquals("自定义消息", result.getMsg());
        assertEquals(100, result.getData());
    }

    @Test
    void testSuccessWithComplexData() {
        // 测试复杂对象数据
        TestData testData = new TestData("test", 123);
        BasicResultVO<TestData> result = BasicResultVO.success(testData);

        assertNotNull(result);
        assertEquals(RespStatusEnum.SUCCESS.getCode(), result.getStatus());
        assertEquals(testData, result.getData());
        assertEquals("test", result.getData().getName());
        assertEquals(123, result.getData().getValue());
    }

    @Test
    void testFailWithClientBadParameters() {
        // 测试客户端参数错误
        BasicResultVO<Void> result = BasicResultVO.fail(RespStatusEnum.CLIENT_BAD_PARAMETERS);

        assertNotNull(result);
        assertEquals(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), result.getStatus());
        assertEquals(RespStatusEnum.CLIENT_BAD_PARAMETERS.getMsg(), result.getMsg());
    }

    // 测试数据类
    static class TestData {
        private String name;
        private int value;

        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }
}
