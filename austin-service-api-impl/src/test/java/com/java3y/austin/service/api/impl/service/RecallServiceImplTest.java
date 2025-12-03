package com.java3y.austin.service.api.impl.service;

import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.common.pipeline.ProcessContext;
import com.java3y.austin.common.pipeline.ProcessController;
import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.service.api.domain.SendRequest;
import com.java3y.austin.service.api.domain.SendResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RecallServiceImpl 单元测试
 *
 * @author austin
 */
@ExtendWith(MockitoExtension.class)
class RecallServiceImplTest {

    @Mock
    private ProcessController processController;

    @InjectMocks
    private RecallServiceImpl recallService;

    @BeforeEach
    void setUp() {
        // Mock processController的行为
        when(processController.process(any(ProcessContext.class)))
                .thenAnswer(invocation -> {
                    ProcessContext context = invocation.getArgument(0);
                    context.setResponse(BasicResultVO.success());
                    return context;
                });
    }

    @Test
    void testRecallWithValidRequest() {
        // 准备测试数据
        SendRequest sendRequest = new SendRequest();
        sendRequest.setCode("RECALL_CODE");
        sendRequest.setMessageTemplateId(1L);
        
        List<String> recallMessageIds = Arrays.asList("msg1", "msg2", "msg3");
        sendRequest.setRecallMessageIds(recallMessageIds);

        // 执行测试
        SendResponse response = recallService.recall(sendRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(RespStatusEnum.SUCCESS.getCode(), response.getStatus());
        verify(processController, times(1)).process(any(ProcessContext.class));
    }

    @Test
    void testRecallWithNullRequest() {
        // 测试空请求
        SendResponse response = recallService.recall(null);

        // 验证结果
        assertNotNull(response);
        assertEquals(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), response.getStatus());
        assertEquals(RespStatusEnum.CLIENT_BAD_PARAMETERS.getMsg(), response.getMsg());
        verify(processController, never()).process(any());
    }

    @Test
    void testRecallWithSingleMessageId() {
        // 准备测试数据 - 单个消息ID
        SendRequest sendRequest = new SendRequest();
        sendRequest.setCode("RECALL_CODE");
        sendRequest.setMessageTemplateId(1L);
        sendRequest.setRecallMessageIds(Arrays.asList("msg1"));

        // 执行测试
        SendResponse response = recallService.recall(sendRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(RespStatusEnum.SUCCESS.getCode(), response.getStatus());
        verify(processController, times(1)).process(any(ProcessContext.class));
    }

    @Test
    void testRecallWithProcessFailure() {
        // Mock processController返回失败
        when(processController.process(any(ProcessContext.class)))
                .thenAnswer(invocation -> {
                    ProcessContext context = invocation.getArgument(0);
                    context.setResponse(BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR));
                    return context;
                });

        // 准备测试数据
        SendRequest sendRequest = new SendRequest();
        sendRequest.setCode("RECALL_CODE");
        sendRequest.setMessageTemplateId(1L);
        sendRequest.setRecallMessageIds(Arrays.asList("msg1"));

        // 执行测试
        SendResponse response = recallService.recall(sendRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(RespStatusEnum.SERVICE_ERROR.getCode(), response.getStatus());
    }
}
