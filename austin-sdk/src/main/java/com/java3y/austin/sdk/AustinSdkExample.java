package com.java3y.austin.sdk;

import com.java3y.austin.sdk.client.AustinClient;
import com.java3y.austin.sdk.config.AustinConfig;
import com.java3y.austin.service.api.domain.MessageParam;
import com.java3y.austin.service.api.domain.SendRequest;
import com.java3y.austin.service.api.domain.SendResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Austin SDK 使用示例
 *
 * @author 3y
 */
public class AustinSdkExample {

    public static void main(String[] args) {
        // 1. 创建配置
        AustinConfig config = AustinConfig.builder()
                .serverUrl("http://localhost:8080")  // Austin服务地址
                .connectTimeout(10000)               // 连接超时10秒
                .readTimeout(30000)                  // 读取超时30秒
                .enableLog(true)                     // 启用日志
                .maxRetryCount(3)                    // 最大重试3次
                // .appKey("your-app-key")           // 可选：应用Key
                // .appSecret("your-app-secret")     // 可选：应用Secret
                .build();

        // 2. 创建客户端
        AustinClient client = new AustinClient(config);

        try {
            // 3. 发送单个消息示例
            sendSingleMessage(client);

            // 4. 发送带变量的消息示例
            sendMessageWithVariables(client);

            // 5. 撤回消息示例
            // recallMessage(client);

        } finally {
            // 6. 关闭客户端
            client.shutdown();
        }
    }

    /**
     * 发送单个消息示例
     */
    private static void sendSingleMessage(AustinClient client) {
        // 构建消息参数
        MessageParam messageParam = MessageParam.builder()
                .receiver("user123@example.com")  // 接收者（邮箱、手机号等）
                .build();

        // 构建发送请求
        SendRequest sendRequest = SendRequest.builder()
                .code("send")                      // 业务类型：send表示发送
                .messageTemplateId(1L)             // 消息模板ID
                .messageParam(messageParam)
                .build();

        // 发送消息
        SendResponse response = client.send(sendRequest);

        // 处理响应
        System.out.println("发送结果: " + response.getCode());
        System.out.println("响应消息: " + response.getMsg());
        if (response.getData() != null) {
            System.out.println("消息ID: " + response.getData());
        }
    }

    /**
     * 发送带变量的消息示例
     */
    private static void sendMessageWithVariables(AustinClient client) {
        // 构建变量Map（用于模板占位符替换）
        Map<String, String> variables = new HashMap<>();
        variables.put("userName", "张三");
        variables.put("code", "123456");
        variables.put("expireTime", "5分钟");

        // 构建扩展参数
        Map<String, String> extra = new HashMap<>();
        extra.put("source", "web");

        // 构建消息参数
        MessageParam messageParam = MessageParam.builder()
                .bizId("order_20231201_001")      // 业务ID（可选，用于链路追踪）
                .receiver("13800138000")           // 多个接收者用逗号分隔
                .variables(variables)              // 模板变量
                .extra(extra)                      // 扩展参数
                .build();

        // 构建发送请求
        SendRequest sendRequest = SendRequest.builder()
                .code("send")
                .messageTemplateId(2L)             // 短信模板ID
                .messageParam(messageParam)
                .build();

        // 发送消息
        SendResponse response = client.send(sendRequest);

        System.out.println("发送结果: " + response.getCode());
        System.out.println("响应消息: " + response.getMsg());
    }

    /**
     * 撤回消息示例（需要有messageId）
     */
    private static void recallMessage(AustinClient client) {
        // 假设我们要撤回的消息ID
        java.util.List<String> messageIds = new java.util.ArrayList<>();
        messageIds.add("message-id-123");

        // 构建撤回请求
        SendRequest recallRequest = SendRequest.builder()
                .code("recall")                    // 业务类型：recall表示撤回
                .messageTemplateId(1L)             // 模板ID
                .recallMessageIds(messageIds)      // 要撤回的消息ID列表
                .build();

        // 撤回消息
        SendResponse response = client.recall(recallRequest);

        System.out.println("撤回结果: " + response.getCode());
        System.out.println("响应消息: " + response.getMsg());
    }
}
