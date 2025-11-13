package com.java3y.austin.sdk.client;

import com.alibaba.fastjson.JSON;
import com.java3y.austin.sdk.config.AustinConfig;
import com.java3y.austin.sdk.exception.AustinSdkException;
import com.java3y.austin.service.api.domain.BatchSendRequest;
import com.java3y.austin.service.api.domain.SendRequest;
import com.java3y.austin.service.api.domain.SendResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Austin SDK 核心客户端
 *
 * @author 3y
 */
@Slf4j
public class AustinClient {

    private final AustinConfig config;
    private final OkHttpClient httpClient;

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final String SEND_PATH = "/send";
    private static final String BATCH_SEND_PATH = "/batchSend";
    private static final String RECALL_PATH = "/recall";

    /**
     * 构造函数
     *
     * @param config Austin配置
     */
    public AustinClient(AustinConfig config) {
        config.validate();
        this.config = config;
        this.httpClient = buildHttpClient();
        if (config.getEnableLog()) {
            log.info("Austin SDK初始化成功, serverUrl: {}", config.getServerUrl());
        }
    }

    /**
     * 构建HTTP客户端
     */
    private OkHttpClient buildHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true);

        // 可选：添加拦截器用于日志、鉴权等
        if (config.getEnableLog()) {
            builder.addInterceptor(new LoggingInterceptor());
        }

        if (config.getAppKey() != null && config.getAppSecret() != null) {
            builder.addInterceptor(new AuthInterceptor(config.getAppKey(), config.getAppSecret()));
        }

        return builder.build();
    }

    /**
     * 单文案发送接口
     *
     * @param sendRequest 发送请求
     * @return 发送响应
     */
    public SendResponse send(SendRequest sendRequest) {
        return send(sendRequest, 0);
    }

    /**
     * 单文案发送接口（支持重试）
     */
    private SendResponse send(SendRequest sendRequest, int retryCount) {
        try {
            String requestJson = JSON.toJSONString(sendRequest);
            String url = config.getServerUrl() + SEND_PATH;

            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, requestJson);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new AustinSdkException("HTTP_ERROR", "HTTP请求失败，状态码: " + response.code());
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                return JSON.parseObject(responseBody, SendResponse.class);
            }
        } catch (IOException e) {
            // 重试逻辑
            if (retryCount < config.getMaxRetryCount()) {
                if (config.getEnableLog()) {
                    log.warn("发送消息失败，正在进行第{}次重试", retryCount + 1);
                }
                return send(sendRequest, retryCount + 1);
            }
            throw new AustinSdkException("SEND_ERROR", "发送消息失败", e);
        } catch (Exception e) {
            throw new AustinSdkException("SEND_ERROR", "发送消息异常", e);
        }
    }

    /**
     * 批量发送接口
     *
     * @param batchSendRequest 批量发送请求
     * @return 发送响应
     */
    public SendResponse batchSend(BatchSendRequest batchSendRequest) {
        return batchSend(batchSendRequest, 0);
    }

    /**
     * 批量发送接口（支持重试）
     */
    private SendResponse batchSend(BatchSendRequest batchSendRequest, int retryCount) {
        try {
            String requestJson = JSON.toJSONString(batchSendRequest);
            String url = config.getServerUrl() + BATCH_SEND_PATH;

            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, requestJson);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new AustinSdkException("HTTP_ERROR", "HTTP请求失败，状态码: " + response.code());
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                return JSON.parseObject(responseBody, SendResponse.class);
            }
        } catch (IOException e) {
            if (retryCount < config.getMaxRetryCount()) {
                if (config.getEnableLog()) {
                    log.warn("批量发送消息失败，正在进行第{}次重试", retryCount + 1);
                }
                return batchSend(batchSendRequest, retryCount + 1);
            }
            throw new AustinSdkException("BATCH_SEND_ERROR", "批量发送消息失败", e);
        } catch (Exception e) {
            throw new AustinSdkException("BATCH_SEND_ERROR", "批量发送消息异常", e);
        }
    }

    /**
     * 撤回消息接口
     *
     * @param sendRequest 撤回请求
     * @return 撤回响应
     */
    public SendResponse recall(SendRequest sendRequest) {
        return recall(sendRequest, 0);
    }

    /**
     * 撤回消息接口（支持重试）
     */
    private SendResponse recall(SendRequest sendRequest, int retryCount) {
        try {
            String requestJson = JSON.toJSONString(sendRequest);
            String url = config.getServerUrl() + RECALL_PATH;

            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, requestJson);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new AustinSdkException("HTTP_ERROR", "HTTP请求失败，状态码: " + response.code());
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                return JSON.parseObject(responseBody, SendResponse.class);
            }
        } catch (IOException e) {
            if (retryCount < config.getMaxRetryCount()) {
                if (config.getEnableLog()) {
                    log.warn("撤回消息失败，正在进行第{}次重试", retryCount + 1);
                }
                return recall(sendRequest, retryCount + 1);
            }
            throw new AustinSdkException("RECALL_ERROR", "撤回消息失败", e);
        } catch (Exception e) {
            throw new AustinSdkException("RECALL_ERROR", "撤回消息异常", e);
        }
    }

    /**
     * 关闭客户端，释放资源
     */
    public void shutdown() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }

    /**
     * 日志拦截器
     */
    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long startTime = System.currentTimeMillis();

            log.info("发送请求: {} {}", request.method(), request.url());

            Response response = chain.proceed(request);

            long endTime = System.currentTimeMillis();
            log.info("收到响应: {} {} 耗时: {}ms", response.code(), request.url(), (endTime - startTime));

            return response;
        }
    }

    /**
     * 鉴权拦截器
     */
    private static class AuthInterceptor implements Interceptor {
        private final String appKey;
        private final String appSecret;

        public AuthInterceptor(String appKey, String appSecret) {
            this.appKey = appKey;
            this.appSecret = appSecret;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();

            // 在请求头中添加鉴权信息
            Request newRequest = originalRequest.newBuilder()
                    .header("X-App-Key", appKey)
                    .header("X-App-Secret", appSecret)
                    .build();

            return chain.proceed(newRequest);
        }
    }
}
