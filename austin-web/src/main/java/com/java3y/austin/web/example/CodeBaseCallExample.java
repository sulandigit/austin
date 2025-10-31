package com.java3y.austin.web.example;

import com.java3y.austin.service.api.domain.MessageParam;
import com.java3y.austin.service.api.domain.SendRequest;
import com.java3y.austin.service.api.domain.SendResponse;
import com.java3y.austin.service.api.domain.BatchSendRequest;
import com.java3y.austin.service.api.service.SendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Codebase调用示例
 * 展示如何在项目中调用SendService发送消息
 *
 * @author example
 */
@Slf4j
@Component
public class CodeBaseCallExample {

    @Autowired
    private SendService sendService;

    /**
     * 示例1: 单文案发送调用
     * 发送单条消息给指定接收者
     */
    public void singleSendExample() {
        // 1. 构建消息参数
        MessageParam messageParam = MessageParam.builder()
                .receiver("user@example.com")  // 接收者
                .variables(buildVariables())    // 消息变量
                .extra("extra_info")            // 额外信息
                .build();

        // 2. 构建发送请求
        SendRequest sendRequest = SendRequest.builder()
                .code("send")                   // 业务代码
                .messageTemplateId(1L)          // 消息模板ID
                .messageParam(messageParam)     // 消息参数
                .build();

        // 3. 调用发送服务
        SendResponse response = sendService.send(sendRequest);

        // 4. 处理响应结果
        if ("0".equals(response.getCode())) {
            log.info("消息发送成功: {}", response.getData());
        } else {
            log.error("消息发送失败: code={}, msg={}", response.getCode(), response.getMsg());
        }
    }

    /**
     * 示例2: 批量发送调用
     * 批量发送消息给多个接收者
     */
    public void batchSendExample() {
        // 1. 构建多个消息参数
        MessageParam messageParam1 = MessageParam.builder()
                .receiver("user1@example.com")
                .variables(buildVariables())
                .build();

        MessageParam messageParam2 = MessageParam.builder()
                .receiver("user2@example.com")
                .variables(buildVariables())
                .build();

        MessageParam messageParam3 = MessageParam.builder()
                .receiver("user3@example.com")
                .variables(buildVariables())
                .build();

        List<MessageParam> messageParamList = Arrays.asList(
                messageParam1, 
                messageParam2, 
                messageParam3
        );

        // 2. 构建批量发送请求
        BatchSendRequest batchSendRequest = BatchSendRequest.builder()
                .code("batch_send")             // 业务代码
                .messageTemplateId(1L)          // 消息模板ID
                .messageParamList(messageParamList) // 消息参数列表
                .build();

        // 3. 调用批量发送服务
        SendResponse response = sendService.batchSend(batchSendRequest);

        // 4. 处理响应结果
        if ("0".equals(response.getCode())) {
            log.info("批量消息发送成功，发送数量: {}", response.getData().size());
            response.getData().forEach(taskInfo -> {
                log.info("任务信息 - businessId: {}, messageId: {}", 
                        taskInfo.getBusinessId(), 
                        taskInfo.getMessageId());
            });
        } else {
            log.error("批量消息发送失败: code={}, msg={}", response.getCode(), response.getMsg());
        }
    }

    /**
     * 示例3: 发送邮件消息
     */
    public void sendEmailExample() {
        Map<String, String> variables = new HashMap<>();
        variables.put("title", "系统通知");
        variables.put("content", "您有一条新的消息");
        variables.put("userName", "张三");

        MessageParam messageParam = MessageParam.builder()
                .receiver("zhangsan@example.com")
                .variables(variables)
                .build();

        SendRequest sendRequest = SendRequest.builder()
                .code("send")
                .messageTemplateId(17L)  // 邮件模板ID
                .messageParam(messageParam)
                .build();

        SendResponse response = sendService.send(sendRequest);
        handleResponse(response);
    }

    /**
     * 示例4: 发送短信消息
     */
    public void sendSmsExample() {
        Map<String, String> variables = new HashMap<>();
        variables.put("code", "123456");
        variables.put("time", "5");

        MessageParam messageParam = MessageParam.builder()
                .receiver("13800138000")  // 手机号
                .variables(variables)
                .build();

        SendRequest sendRequest = SendRequest.builder()
                .code("send")
                .messageTemplateId(20L)  // 短信模板ID
                .messageParam(messageParam)
                .build();

        SendResponse response = sendService.send(sendRequest);
        handleResponse(response);
    }

    /**
     * 构建消息变量
     */
    private Map<String, String> buildVariables() {
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "示例用户");
        variables.put("content", "这是一条测试消息");
        variables.put("time", String.valueOf(System.currentTimeMillis()));
        return variables;
    }

    /**
     * 统一处理响应结果
     */
    private void handleResponse(SendResponse response) {
        if (response == null) {
            log.error("发送响应为空");
            return;
        }

        if ("0".equals(response.getCode())) {
            log.info("消息发送成功");
            if (response.getData() != null && !response.getData().isEmpty()) {
                response.getData().forEach(taskInfo -> {
                    log.info("messageId: {}, businessId: {}, bizId: {}",
                            taskInfo.getMessageId(),
                            taskInfo.getBusinessId(),
                            taskInfo.getBizId());
                });
            }
        } else {
            log.error("消息发送失败 - code: {}, msg: {}", response.getCode(), response.getMsg());
        }
    }

    /**
     * 完整的调用示例
     * 展示从构建请求到处理响应的完整流程
     */
    public void completeCallExample() {
        log.info("开始执行完整的codebase调用示例");

        try {
            // Step 1: 准备消息参数
            MessageParam messageParam = MessageParam.builder()
                    .receiver("test@example.com")
                    .variables(buildVariables())
                    .extra("业务扩展信息")
                    .build();

            // Step 2: 构建发送请求
            SendRequest sendRequest = SendRequest.builder()
                    .code("send")
                    .messageTemplateId(1L)
                    .messageParam(messageParam)
                    .build();

            // Step 3: 调用服务
            log.info("调用SendService发送消息: templateId={}, receiver={}", 
                    sendRequest.getMessageTemplateId(), 
                    messageParam.getReceiver());
            
            SendResponse response = sendService.send(sendRequest);

            // Step 4: 处理结果
            handleResponse(response);

        } catch (Exception e) {
            log.error("发送消息异常", e);
        }

        log.info("完整的codebase调用示例执行结束");
    }
}
