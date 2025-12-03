package com.java3y.austin.service.api.impl.service;

import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.common.pipeline.ProcessContext;
import com.java3y.austin.common.pipeline.ProcessController;
import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.service.api.domain.BatchSendRequest;
import com.java3y.austin.service.api.domain.MessageParam;
import com.java3y.austin.service.api.domain.SendRequest;
import com.java3y.austin.service.api.domain.SendResponse;
import com.java3y.austin.service.api.impl.domain.SendTaskModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SendServiceImpl 单元测试
 *
 * @author austin
 */
@ExtendWith(MockitoExtension.class)
class SendServiceImplCompleteTest {

    @Mock
    private ProcessController processController;

    @InjectMocks
    private SendServiceImpl sendService;

    @BeforeEach
    void setUp() {
        // Mock processController的行为
        when(processController.process(any(ProcessContext.class)))
                .thenAnswer(invocation -> {
                    ProcessContext context = invocation.getArgument(0);
                    // 模拟成功的处理
                    context.setResponse(BasicResultVO.success(new ArrayList<>()));
                    return context;
                });
    }

    @Test
    void testSendWithValidRequest() {
        // 准备测试数据
        MessageParam messageParam = new MessageParam();
        messageParam.setReceiver("user1");
        messageParam.setVariables(new HashMap<>());

        SendRequest sendRequest = new SendRequest();
        sendRequest.setCode("SEND_CODE");
        sendRequest.setMessageTemplateId(1L);
        sendRequest.setMessageParam(messageParam);

        // 执行测试
        SendResponse response = sendService.send(sendRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(RespStatusEnum.SUCCESS.getCode(), response.getStatus());
        verify(processController, times(1)).process(any(ProcessContext.class));
    }

    @Test
    void testSendWithNullRequest() {
        // 测试空请求
        SendResponse response = sendService.send(null);

        // 验证结果
        assertNotNull(response);
        assertEquals(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), response.getStatus());
        assertEquals(RespStatusEnum.CLIENT_BAD_PARAMETERS.getMsg(), response.getMsg());
        verify(processController, never()).process(any());
    }

    @Test
    void testBatchSendWithValidRequest() {
        // 准备测试数据
        List<MessageParam> messageParamList = new ArrayList<>();
        
        MessageParam param1 = new MessageParam();
        param1.setReceiver("user1");
        param1.setVariables(new HashMap<>());
        
        MessageParam param2 = new MessageParam();
        param2.setReceiver("user2");
        param2.setVariables(new HashMap<>());
        
        messageParamList.add(param1);
        messageParamList.add(param2);

        BatchSendRequest batchSendRequest = new BatchSendRequest();
        batchSendRequest.setCode("BATCH_SEND_CODE");
        batchSendRequest.setMessageTemplateId(1L);
        batchSendRequest.setMessageParamList(messageParamList);

        // 执行测试
        SendResponse response = sendService.batchSend(batchSendRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(RespStatusEnum.SUCCESS.getCode(), response.getStatus());
        verify(processController, times(1)).process(any(ProcessContext.class));
    }

    @Test
    void testBatchSendWithNullRequest() {
        // 测试空请求
        SendResponse response = sendService.batchSend(null);

        // 验证结果
        assertNotNull(response);
        assertEquals(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), response.getStatus());
        assertEquals(RespStatusEnum.CLIENT_BAD_PARAMETERS.getMsg(), response.getMsg());
        verify(processController, never()).process(any());
    }

    @Test
    void testSendWithProcessFailure() {
        // Mock processController返回失败
        when(processController.process(any(ProcessContext.class)))
                .thenAnswer(invocation -> {
                    ProcessContext context = invocation.getArgument(0);
                    context.setResponse(BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR));
                    return context;
                });

        // 准备测试数据
        MessageParam messageParam = new MessageParam();
        messageParam.setReceiver("user1");
        messageParam.setVariables(new HashMap<>());

        SendRequest sendRequest = new SendRequest();
        sendRequest.setCode("SEND_CODE");
        sendRequest.setMessageTemplateId(1L);
        sendRequest.setMessageParam(messageParam);

        // 执行测试
        SendResponse response = sendService.send(sendRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(RespStatusEnum.SERVICE_ERROR.getCode(), response.getStatus());
    }

    @Test
    void testBatchSendWithEmptyList() {
        // 准备测试数据 - 空列表
        BatchSendRequest batchSendRequest = new BatchSendRequest();
        batchSendRequest.setCode("BATCH_SEND_CODE");
        batchSendRequest.setMessageTemplateId(1L);
        batchSendRequest.setMessageParamList(new ArrayList<>());

        // 执行测试
        SendResponse response = sendService.batchSend(batchSendRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(RespStatusEnum.SUCCESS.getCode(), response.getStatus());
        verify(processController, times(1)).process(any(ProcessContext.class));
    }

    @Test
    void testBatchSendWithMultipleParams() {
        // 准备测试数据 - 多个参数
        List<MessageParam> messageParamList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MessageParam param = new MessageParam();
            param.setReceiver("user" + i);
            param.setVariables(new HashMap<>());
            messageParamList.add(param);
        }

        BatchSendRequest batchSendRequest = new BatchSendRequest();
        batchSendRequest.setCode("BATCH_SEND_CODE");
        batchSendRequest.setMessageTemplateId(1L);
        batchSendRequest.setMessageParamList(messageParamList);

        // 执行测试
        SendResponse response = sendService.batchSend(batchSendRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(RespStatusEnum.SUCCESS.getCode(), response.getStatus());
        verify(processController, times(1)).process(any(ProcessContext.class));
    }
}
