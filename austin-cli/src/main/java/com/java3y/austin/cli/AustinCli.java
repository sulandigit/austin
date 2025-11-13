package com.java3y.austin.cli;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.java3y.austin.sdk.client.AustinClient;
import com.java3y.austin.sdk.config.AustinConfig;
import com.java3y.austin.service.api.domain.BatchSendRequest;
import com.java3y.austin.service.api.domain.MessageParam;
import com.java3y.austin.service.api.domain.SendRequest;
import com.java3y.austin.service.api.domain.SendResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Austin CLI - 命令行工具
 *
 * @author 3y
 */
@Command(name = "austin-cli",
        mixinStandardHelpOptions = true,
        version = "austin-cli 1.0",
        description = "Austin消息推送平台命令行工具")
public class AustinCli implements Callable<Integer> {

    @Option(names = {"-s", "--server"}, description = "Austin服务地址", required = true)
    private String serverUrl;

    @Option(names = {"-t", "--template"}, description = "消息模板ID", required = true)
    private Long templateId;

    @Option(names = {"-r", "--receiver"}, description = "接收者（多个用逗号分隔）", required = true)
    private String receiver;

    @Option(names = {"-v", "--variables"}, description = "模板变量（JSON格式）")
    private String variables;

    @Option(names = {"-b", "--bizId"}, description = "业务ID")
    private String bizId;

    @Option(names = {"-f", "--file"}, description = "批量发送配置文件（JSON格式）")
    private String configFile;

    @Option(names = {"-c", "--code"}, description = "业务类型（send/recall）", defaultValue = "send")
    private String code;

    @Option(names = {"--batch"}, description = "批量发送模式")
    private boolean batchMode;

    @Option(names = {"--timeout"}, description = "超时时间（毫秒）", defaultValue = "30000")
    private Integer timeout;

    @Option(names = {"--retry"}, description = "重试次数", defaultValue = "3")
    private Integer retryCount;

    @Option(names = {"--verbose"}, description = "详细输出")
    private boolean verbose;

    @Override
    public Integer call() throws Exception {
        try {
            // 创建Austin客户端
            AustinConfig config = AustinConfig.builder()
                    .serverUrl(serverUrl)
                    .readTimeout(timeout)
                    .enableLog(verbose)
                    .maxRetryCount(retryCount)
                    .build();

            AustinClient client = new AustinClient(config);

            try {
                SendResponse response;

                if (configFile != null && !configFile.isEmpty()) {
                    // 从文件读取配置
                    response = sendFromFile(client, configFile);
                } else if (batchMode) {
                    // 批量发送模式
                    response = sendBatch(client);
                } else {
                    // 单条发送模式
                    response = sendSingle(client);
                }

                // 输出结果
                printResponse(response);

                return "0".equals(response.getCode()) ? 0 : 1;
            } finally {
                client.shutdown();
            }
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    /**
     * 单条发送
     */
    private SendResponse sendSingle(AustinClient client) {
        Map<String, String> variableMap = parseVariables(variables);

        MessageParam messageParam = MessageParam.builder()
                .bizId(bizId)
                .receiver(receiver)
                .variables(variableMap)
                .build();

        SendRequest sendRequest = SendRequest.builder()
                .code(code)
                .messageTemplateId(templateId)
                .messageParam(messageParam)
                .build();

        return client.send(sendRequest);
    }

    /**
     * 批量发送
     */
    private SendResponse sendBatch(AustinClient client) {
        String[] receivers = receiver.split(",");
        List<MessageParam> messageParamList = new ArrayList<>();

        Map<String, String> variableMap = parseVariables(variables);

        for (String rec : receivers) {
            MessageParam param = MessageParam.builder()
                    .receiver(rec.trim())
                    .variables(variableMap)
                    .build();
            messageParamList.add(param);
        }

        BatchSendRequest batchRequest = BatchSendRequest.builder()
                .code(code)
                .messageTemplateId(templateId)
                .messageParamList(messageParamList)
                .build();

        return client.batchSend(batchRequest);
    }

    /**
     * 从文件发送
     */
    private SendResponse sendFromFile(AustinClient client, String filePath) {
        String content = FileUtil.readUtf8String(new File(filePath));
        
        if (content.contains("messageParamList")) {
            // 批量发送
            BatchSendRequest batchRequest = JSON.parseObject(content, BatchSendRequest.class);
            return client.batchSend(batchRequest);
        } else {
            // 单条发送
            SendRequest sendRequest = JSON.parseObject(content, SendRequest.class);
            return client.send(sendRequest);
        }
    }

    /**
     * 解析变量
     */
    private Map<String, String> parseVariables(String vars) {
        if (vars == null || vars.isEmpty()) {
            return null;
        }
        try {
            return JSON.parseObject(vars, Map.class);
        } catch (Exception e) {
            System.err.println("警告: 无法解析变量JSON，忽略变量参数");
            return null;
        }
    }

    /**
     * 打印响应结果
     */
    private void printResponse(SendResponse response) {
        System.out.println("\n========== 发送结果 ==========");
        System.out.println("状态码: " + response.getCode());
        System.out.println("消息: " + response.getMsg());
        
        if (response.getData() != null && !response.getData().isEmpty()) {
            System.out.println("\n发送详情:");
            response.getData().forEach(task -> {
                System.out.println("  - 消息ID: " + task.getMessageId());
                System.out.println("    业务ID: " + task.getBizId());
                System.out.println("    业务模板ID: " + task.getBusinessId());
            });
        }
        System.out.println("==============================\n");
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new AustinCli()).execute(args);
        System.exit(exitCode);
    }
}
