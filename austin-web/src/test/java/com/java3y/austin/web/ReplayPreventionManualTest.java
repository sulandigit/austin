package com.java3y.austin.web;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 防重放机制手动验证程序
 * 可以直接运行main方法进行验证
 *
 * @author austin
 */
public class ReplayPreventionManualTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String SECRET = "austin-anti-replay-secret-key-2024";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("防重放机制验证程序");
        System.out.println("========================================\n");

        // 测试1: 正常请求
        test1_NormalRequest();

        // 等待1秒
        sleep(1000);

        // 测试2: 重放攻击
        test2_ReplayAttack();

        // 等待1秒
        sleep(1000);

        // 测试3: 过期时间戳
        test3_ExpiredTimestamp();

        // 等待1秒
        sleep(1000);

        // 测试4: 错误签名
        test4_InvalidSign();

        // 等待1秒
        sleep(1000);

        // 测试5: 缺少参数
        test5_MissingParameters();

        System.out.println("\n========================================");
        System.out.println("所有测试完成！");
        System.out.println("========================================");
    }

    /**
     * 测试1: 正常请求
     */
    private static void test1_NormalRequest() {
        System.out.println("【测试1】正常请求");
        System.out.println("------------------------------------------");

        long timestamp = System.currentTimeMillis();
        String nonce = generateNonce();
        String code = "test_template";

        Map<String, String> params = new HashMap<>();
        params.put("code", code);

        String sign = calculateSign(params, String.valueOf(timestamp), nonce);

        System.out.println("timestamp: " + timestamp);
        System.out.println("nonce: " + nonce);
        System.out.println("sign: " + sign);

        try {
            HttpResponse response = HttpRequest.post(BASE_URL + "/send")
                    .header("timestamp", String.valueOf(timestamp))
                    .header("nonce", nonce)
                    .header("sign", sign)
                    .header("Content-Type", "application/json")
                    .body("{\"code\":\"" + code + "\"}")
                    .execute();

            System.out.println("响应状态: " + response.getStatus());
            System.out.println("响应内容: " + response.body());
            System.out.println("✓ 测试通过 - 正常请求应该成功\n");
        } catch (Exception e) {
            System.out.println("✗ 测试失败: " + e.getMessage() + "\n");
        }
    }

    /**
     * 测试2: 重放攻击
     */
    private static void test2_ReplayAttack() {
        System.out.println("【测试2】重放攻击");
        System.out.println("------------------------------------------");

        long timestamp = System.currentTimeMillis();
        String nonce = generateNonce();
        String code = "test_replay";

        Map<String, String> params = new HashMap<>();
        params.put("code", code);

        String sign = calculateSign(params, String.valueOf(timestamp), nonce);

        System.out.println("使用相同的timestamp和sign发送两次请求...");
        System.out.println("timestamp: " + timestamp);
        System.out.println("nonce: " + nonce);
        System.out.println("sign: " + sign);

        try {
            // 第一次请求
            System.out.println("\n第一次请求:");
            HttpResponse response1 = HttpRequest.post(BASE_URL + "/send")
                    .header("timestamp", String.valueOf(timestamp))
                    .header("nonce", nonce)
                    .header("sign", sign)
                    .header("Content-Type", "application/json")
                    .body("{\"code\":\"" + code + "\"}")
                    .execute();
            System.out.println("响应: " + response1.body());

            // 等待500ms
            sleep(500);

            // 第二次请求（重放）
            System.out.println("\n第二次请求（重放）:");
            HttpResponse response2 = HttpRequest.post(BASE_URL + "/send")
                    .header("timestamp", String.valueOf(timestamp))
                    .header("nonce", nonce)
                    .header("sign", sign)
                    .header("Content-Type", "application/json")
                    .body("{\"code\":\"" + code + "\"}")
                    .execute();
            System.out.println("响应: " + response2.body());

            if (response2.body().contains("重复提交") || response2.body().contains("已被处理")) {
                System.out.println("✓ 测试通过 - 重放请求被成功拦截\n");
            } else {
                System.out.println("✗ 测试失败 - 重放请求未被拦截\n");
            }
        } catch (Exception e) {
            System.out.println("✗ 测试失败: " + e.getMessage() + "\n");
        }
    }

    /**
     * 测试3: 过期的时间戳
     */
    private static void test3_ExpiredTimestamp() {
        System.out.println("【测试3】过期的时间戳");
        System.out.println("------------------------------------------");

        // 使用6分钟前的时间戳
        long timestamp = System.currentTimeMillis() - (6 * 60 * 1000);
        String nonce = generateNonce();
        String code = "test_expired";

        Map<String, String> params = new HashMap<>();
        params.put("code", code);

        String sign = calculateSign(params, String.valueOf(timestamp), nonce);

        System.out.println("timestamp: " + timestamp + " (6分钟前)");
        System.out.println("nonce: " + nonce);
        System.out.println("sign: " + sign);

        try {
            HttpResponse response = HttpRequest.post(BASE_URL + "/send")
                    .header("timestamp", String.valueOf(timestamp))
                    .header("nonce", nonce)
                    .header("sign", sign)
                    .header("Content-Type", "application/json")
                    .body("{\"code\":\"" + code + "\"}")
                    .execute();

            System.out.println("响应: " + response.body());

            if (response.body().contains("过期") || response.body().contains("时间戳")) {
                System.out.println("✓ 测试通过 - 过期时间戳被拦截\n");
            } else {
                System.out.println("✗ 测试失败 - 过期时间戳未被拦截\n");
            }
        } catch (Exception e) {
            System.out.println("✗ 测试失败: " + e.getMessage() + "\n");
        }
    }

    /**
     * 测试4: 错误的签名
     */
    private static void test4_InvalidSign() {
        System.out.println("【测试4】错误的签名");
        System.out.println("------------------------------------------");

        long timestamp = System.currentTimeMillis();
        String nonce = generateNonce();
        String code = "test_invalid_sign";
        String invalidSign = "invalid_sign_12345678";

        System.out.println("timestamp: " + timestamp);
        System.out.println("nonce: " + nonce);
        System.out.println("sign: " + invalidSign + " (错误的签名)");

        try {
            HttpResponse response = HttpRequest.post(BASE_URL + "/send")
                    .header("timestamp", String.valueOf(timestamp))
                    .header("nonce", nonce)
                    .header("sign", invalidSign)
                    .header("Content-Type", "application/json")
                    .body("{\"code\":\"" + code + "\"}")
                    .execute();

            System.out.println("响应: " + response.body());

            if (response.body().contains("签名") || response.body().contains("验证失败")) {
                System.out.println("✓ 测试通过 - 错误签名被拦截\n");
            } else {
                System.out.println("✗ 测试失败 - 错误签名未被拦截\n");
            }
        } catch (Exception e) {
            System.out.println("✗ 测试失败: " + e.getMessage() + "\n");
        }
    }

    /**
     * 测试5: 缺少必要参数
     */
    private static void test5_MissingParameters() {
        System.out.println("【测试5】缺少必要参数");
        System.out.println("------------------------------------------");

        String code = "test_missing_params";

        System.out.println("发送请求时缺少timestamp参数...");

        try {
            HttpResponse response = HttpRequest.post(BASE_URL + "/send")
                    .header("nonce", "test_nonce")
                    .header("sign", "test_sign")
                    .header("Content-Type", "application/json")
                    .body("{\"code\":\"" + code + "\"}")
                    .execute();

            System.out.println("响应: " + response.body());

            if (response.body().contains("时间戳") || response.body().contains("timestamp")) {
                System.out.println("✓ 测试通过 - 缺少参数被拦截\n");
            } else {
                System.out.println("✗ 测试失败 - 缺少参数未被拦截\n");
            }
        } catch (Exception e) {
            System.out.println("✗ 测试失败: " + e.getMessage() + "\n");
        }
    }

    /**
     * 计算签名
     */
    private static String calculateSign(Map<String, String> params, String timestamp, String nonce) {
        // 按key排序
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        // 拼接参数
        StringBuilder signBuilder = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            if (value != null && !value.isEmpty()) {
                if (signBuilder.length() > 0) {
                    signBuilder.append("&");
                }
                signBuilder.append(key).append("=").append(value);
            }
        }

        // 追加timestamp
        if (timestamp != null && !timestamp.isEmpty()) {
            if (signBuilder.length() > 0) {
                signBuilder.append("&");
            }
            signBuilder.append("timestamp=").append(timestamp);
        }

        // 追加nonce
        if (nonce != null && !nonce.isEmpty()) {
            if (signBuilder.length() > 0) {
                signBuilder.append("&");
            }
            signBuilder.append("nonce=").append(nonce);
        }

        String signContent = signBuilder.toString();

        // 使用HMAC-SHA256计算签名
        HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, SECRET.getBytes(StandardCharsets.UTF_8));
        return hmac.digestHex(signContent);
    }

    /**
     * 生成随机nonce
     */
    private static String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 休眠
     */
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
