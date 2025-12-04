package com.java3y.austin.support.utils;

import com.java3y.austin.common.constant.SignatureConstant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 签名工具类测试
 *
 * @author austin
 */
public class SignatureUtilsTest {

    @Test
    public void testGenerateSignature() {
        // 测试数据
        String appId = "test-app-001";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = "abcd1234efgh5678";
        String method = "POST";
        String path = "/send";
        String body = "{\"code\":\"send\",\"messageTemplateId\":1}";
        String appSecret = "test-secret-key-123456";
        String version = SignatureConstant.SIGN_VERSION_V1;

        // 生成签名
        String signature = SignatureUtils.generateSignature(
                appId, timestamp, nonce, method, path, body, appSecret, version
        );

        // 验证签名不为空
        assertNotNull(signature);
        assertTrue(signature.length() > 0);
        
        // 验证签名是十六进制字符串
        assertTrue(signature.matches("^[a-f0-9]+$"));

        System.out.println("生成的签名: " + signature);
    }

    @Test
    public void testGenerateSignatureConsistency() {
        // 相同输入应该生成相同签名
        String appId = "test-app-001";
        String timestamp = "1701676800";
        String nonce = "abcd1234efgh5678";
        String method = "POST";
        String path = "/send";
        String body = "{\"code\":\"send\",\"messageTemplateId\":1}";
        String appSecret = "test-secret-key-123456";
        String version = SignatureConstant.SIGN_VERSION_V1;

        String signature1 = SignatureUtils.generateSignature(
                appId, timestamp, nonce, method, path, body, appSecret, version
        );

        String signature2 = SignatureUtils.generateSignature(
                appId, timestamp, nonce, method, path, body, appSecret, version
        );

        assertEquals(signature1, signature2, "相同输入应该生成相同签名");
    }

    @Test
    public void testIsValidNonce() {
        // 有效的nonce
        assertTrue(SignatureUtils.isValidNonce("abcd1234efgh5678"));
        assertTrue(SignatureUtils.isValidNonce("1234567890123456"));
        assertTrue(SignatureUtils.isValidNonce("ABCDEFGHIJKLMNOP"));
        
        // 长度为16-32之间
        assertTrue(SignatureUtils.isValidNonce("1234567890123456"));
        assertTrue(SignatureUtils.isValidNonce("12345678901234567890123456789012"));

        // 无效的nonce
        assertFalse(SignatureUtils.isValidNonce(""));
        assertFalse(SignatureUtils.isValidNonce(null));
        assertFalse(SignatureUtils.isValidNonce("123")); // 太短
        assertFalse(SignatureUtils.isValidNonce("123456789012345678901234567890123")); // 太长
        assertFalse(SignatureUtils.isValidNonce("abcd-1234-efgh")); // 包含特殊字符
        assertFalse(SignatureUtils.isValidNonce("abcd 1234 efgh")); // 包含空格
    }

    @Test
    public void testIsValidTimestamp() {
        // 当前时间戳应该有效
        long currentTimestamp = System.currentTimeMillis() / 1000;
        assertTrue(SignatureUtils.isValidTimestamp(currentTimestamp));

        // 5分钟前的时间戳应该有效
        long fiveMinutesAgo = currentTimestamp - 290;
        assertTrue(SignatureUtils.isValidTimestamp(fiveMinutesAgo));

        // 5分钟后的时间戳应该有效
        long fiveMinutesLater = currentTimestamp + 290;
        assertTrue(SignatureUtils.isValidTimestamp(fiveMinutesLater));

        // 超过5分钟的时间戳应该无效
        long tenMinutesAgo = currentTimestamp - 600;
        assertFalse(SignatureUtils.isValidTimestamp(tenMinutesAgo));

        long tenMinutesLater = currentTimestamp + 600;
        assertFalse(SignatureUtils.isValidTimestamp(tenMinutesLater));
    }

    @Test
    public void testSignatureWithEmptyBody() {
        // 测试空请求体的签名
        String appId = "test-app-001";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = "abcd1234efgh5678";
        String method = "GET";
        String path = "/query";
        String body = "";
        String appSecret = "test-secret-key-123456";
        String version = SignatureConstant.SIGN_VERSION_V1;

        String signature = SignatureUtils.generateSignature(
                appId, timestamp, nonce, method, path, body, appSecret, version
        );

        assertNotNull(signature);
        assertTrue(signature.length() > 0);
    }

    @Test
    public void testSignatureDifferentBody() {
        // 不同的请求体应该生成不同的签名
        String appId = "test-app-001";
        String timestamp = "1701676800";
        String nonce = "abcd1234efgh5678";
        String method = "POST";
        String path = "/send";
        String appSecret = "test-secret-key-123456";
        String version = SignatureConstant.SIGN_VERSION_V1;

        String body1 = "{\"code\":\"send\",\"messageTemplateId\":1}";
        String signature1 = SignatureUtils.generateSignature(
                appId, timestamp, nonce, method, path, body1, appSecret, version
        );

        String body2 = "{\"code\":\"send\",\"messageTemplateId\":2}";
        String signature2 = SignatureUtils.generateSignature(
                appId, timestamp, nonce, method, path, body2, appSecret, version
        );

        assertNotEquals(signature1, signature2, "不同的请求体应该生成不同的签名");
    }

}
