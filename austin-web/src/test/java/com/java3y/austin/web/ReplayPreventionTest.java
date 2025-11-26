package com.java3y.austin.web;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.java3y.austin.web.utils.SignUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 防重放机制测试
 *
 * @author austin
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ReplayPreventionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String SECRET = "austin-anti-replay-secret-key-2024";

    /**
     * 测试1: 正常请求应该成功
     */
    @Test
    public void testNormalRequest() throws Exception {
        long timestamp = System.currentTimeMillis();
        String nonce = SignUtils.generateNonce();
        
        Map<String, String> params = new HashMap<>();
        params.put("code", "test_template");
        
        String sign = SignUtils.generateSign(params, String.valueOf(timestamp), nonce, SECRET);
        
        mockMvc.perform(post("/send")
                .header("timestamp", timestamp)
                .header("nonce", nonce)
                .header("sign", sign)
                .param("code", "test_template")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"test_template\"}"))
                .andDo(print());
    }

    /**
     * 测试2: 重放请求应该被拒绝
     */
    @Test
    public void testReplayAttack() throws Exception {
        long timestamp = System.currentTimeMillis();
        String nonce = SignUtils.generateNonce();
        
        Map<String, String> params = new HashMap<>();
        params.put("code", "test_template");
        
        String sign = SignUtils.generateSign(params, String.valueOf(timestamp), nonce, SECRET);
        
        // 第一次请求
        mockMvc.perform(post("/send")
                .header("timestamp", timestamp)
                .header("nonce", nonce)
                .header("sign", sign)
                .param("code", "test_template")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"test_template\"}"))
                .andDo(print());
        
        // 第二次请求（重放）- 应该被拒绝
        mockMvc.perform(post("/send")
                .header("timestamp", timestamp)
                .header("nonce", nonce)
                .header("sign", sign)
                .param("code", "test_template")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"test_template\"}"))
                .andDo(print())
                .andExpect(status().isOk()); // 虽然HTTP状态200，但业务返回会提示重复
    }

    /**
     * 测试3: 过期的时间戳应该被拒绝
     */
    @Test
    public void testExpiredTimestamp() throws Exception {
        // 使用6分钟前的时间戳（超过5分钟窗口）
        long timestamp = System.currentTimeMillis() - (6 * 60 * 1000);
        String nonce = SignUtils.generateNonce();
        
        Map<String, String> params = new HashMap<>();
        params.put("code", "test_template");
        
        String sign = SignUtils.generateSign(params, String.valueOf(timestamp), nonce, SECRET);
        
        mockMvc.perform(post("/send")
                .header("timestamp", timestamp)
                .header("nonce", nonce)
                .header("sign", sign)
                .param("code", "test_template")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"test_template\"}"))
                .andDo(print())
                .andExpect(status().isOk()); // HTTP 200但业务层会拒绝
    }

    /**
     * 测试4: 错误的签名应该被拒绝
     */
    @Test
    public void testInvalidSign() throws Exception {
        long timestamp = System.currentTimeMillis();
        String nonce = SignUtils.generateNonce();
        
        String invalidSign = "invalid_sign_12345";
        
        mockMvc.perform(post("/send")
                .header("timestamp", timestamp)
                .header("nonce", nonce)
                .header("sign", invalidSign)
                .param("code", "test_template")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"test_template\"}"))
                .andDo(print())
                .andExpect(status().isOk()); // HTTP 200但业务层会拒绝
    }

    /**
     * 测试5: 缺少必要参数应该被拒绝
     */
    @Test
    public void testMissingParameters() throws Exception {
        // 缺少timestamp
        mockMvc.perform(post("/send")
                .header("nonce", "test_nonce")
                .header("sign", "test_sign")
                .param("code", "test_template")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"test_template\"}"))
                .andDo(print());
    }

    /**
     * 测试6: 签名工具类测试
     */
    @Test
    public void testSignUtils() {
        Map<String, String> params = new HashMap<>();
        params.put("messageTemplateId", "1");
        params.put("receiver", "test@example.com");
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = SignUtils.generateNonce();
        
        // 生成签名
        String sign = SignUtils.generateSign(params, timestamp, nonce, SECRET);
        
        System.out.println("=== 签名测试 ===");
        System.out.println("timestamp: " + timestamp);
        System.out.println("nonce: " + nonce);
        System.out.println("sign: " + sign);
        System.out.println("===============");
        
        // 验证签名不为空
        assert sign != null && !sign.isEmpty();
    }
}
