package com.java3y.austin.service.api.impl.action.send;

import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.common.pipeline.ProcessContext;
import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.service.api.domain.MessageParam;
import com.java3y.austin.service.api.impl.domain.SendTaskModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SendPreCheckAction 单元测试
 *
 * @author austin
 */
class SendPreCheckActionTest {

    private SendPreCheckAction sendPreCheckAction;

    @BeforeEach
    void setUp() {
        sendPreCheckAction = new SendPreCheckAction();
    }

    @Test
    void testProcessWithValidData() {
        // 准备测试数据
        List<MessageParam> messageParamList = new ArrayList<>();
        MessageParam param = new MessageParam();
        param.setReceiver("user1,user2");
        param.setVariables(new HashMap<>());
        messageParamList.add(param);

        SendTaskModel sendTaskModel = SendTaskModel.builder()
                .messageTemplateId(1L)
                .messageParamList(messageParamList)
                .build();

        ProcessContext<SendTaskModel> context = ProcessContext.<SendTaskModel>builder()
                .code("TEST")
                .processModel(sendTaskModel)
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        // 执行测试
        sendPreCheckAction.process(context);

        // 验证结果
        assertFalse(context.getNeedBreak());
        assertEquals(1, sendTaskModel.getMessageParamList().size());
    }

    @Test
    void testProcessWithNullTemplateId() {
        // 准备测试数据 - 没有模板ID
        List<MessageParam> messageParamList = new ArrayList<>();
        MessageParam param = new MessageParam();
        param.setReceiver("user1");
        messageParamList.add(param);

        SendTaskModel sendTaskModel = SendTaskModel.builder()
                .messageTemplateId(null)
                .messageParamList(messageParamList)
                .build();

        ProcessContext<SendTaskModel> context = ProcessContext.<SendTaskModel>builder()
                .code("TEST")
                .processModel(sendTaskModel)
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        // 执行测试
        sendPreCheckAction.process(context);

        // 验证结果
        assertTrue(context.getNeedBreak());
        assertEquals(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), context.getResponse().getStatus());
    }

    @Test
    void testProcessWithEmptyMessageParamList() {
        // 准备测试数据 - 空的参数列表
        SendTaskModel sendTaskModel = SendTaskModel.builder()
                .messageTemplateId(1L)
                .messageParamList(new ArrayList<>())
                .build();

        ProcessContext<SendTaskModel> context = ProcessContext.<SendTaskModel>builder()
                .code("TEST")
                .processModel(sendTaskModel)
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        // 执行测试
        sendPreCheckAction.process(context);

        // 验证结果
        assertTrue(context.getNeedBreak());
        assertEquals(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), context.getResponse().getStatus());
    }

    @Test
    void testProcessWithBlankReceiver() {
        // 准备测试数据 - receiver为空白
        List<MessageParam> messageParamList = new ArrayList<>();
        MessageParam param1 = new MessageParam();
        param1.setReceiver("");
        MessageParam param2 = new MessageParam();
        param2.setReceiver("  ");
        MessageParam param3 = new MessageParam();
        param3.setReceiver(null);
        messageParamList.add(param1);
        messageParamList.add(param2);
        messageParamList.add(param3);

        SendTaskModel sendTaskModel = SendTaskModel.builder()
                .messageTemplateId(1L)
                .messageParamList(messageParamList)
                .build();

        ProcessContext<SendTaskModel> context = ProcessContext.<SendTaskModel>builder()
                .code("TEST")
                .processModel(sendTaskModel)
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        // 执行测试
        sendPreCheckAction.process(context);

        // 验证结果 - 所有receiver都是空的，应该被过滤
        assertTrue(context.getNeedBreak());
        assertEquals(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), context.getResponse().getStatus());
    }

    @Test
    void testProcessWithMixedReceivers() {
        // 准备测试数据 - 混合有效和无效的receiver
        List<MessageParam> messageParamList = new ArrayList<>();
        MessageParam param1 = new MessageParam();
        param1.setReceiver("user1");
        MessageParam param2 = new MessageParam();
        param2.setReceiver("");
        MessageParam param3 = new MessageParam();
        param3.setReceiver("user2");
        messageParamList.add(param1);
        messageParamList.add(param2);
        messageParamList.add(param3);

        SendTaskModel sendTaskModel = SendTaskModel.builder()
                .messageTemplateId(1L)
                .messageParamList(messageParamList)
                .build();

        ProcessContext<SendTaskModel> context = ProcessContext.<SendTaskModel>builder()
                .code("TEST")
                .processModel(sendTaskModel)
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        // 执行测试
        sendPreCheckAction.process(context);

        // 验证结果 - 应该只保留有效的receiver
        assertFalse(context.getNeedBreak());
        assertEquals(2, sendTaskModel.getMessageParamList().size());
    }

    @Test
    void testProcessWithTooManyReceivers() {
        // 准备测试数据 - 超过100个接收者
        List<MessageParam> messageParamList = new ArrayList<>();
        MessageParam param = new MessageParam();
        
        // 创建101个接收者
        StringBuilder receivers = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            if (i > 0) {
                receivers.append(",");
            }
            receivers.append("user").append(i);
        }
        param.setReceiver(receivers.toString());
        messageParamList.add(param);

        SendTaskModel sendTaskModel = SendTaskModel.builder()
                .messageTemplateId(1L)
                .messageParamList(messageParamList)
                .build();

        ProcessContext<SendTaskModel> context = ProcessContext.<SendTaskModel>builder()
                .code("TEST")
                .processModel(sendTaskModel)
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        // 执行测试
        sendPreCheckAction.process(context);

        // 验证结果
        assertTrue(context.getNeedBreak());
        assertEquals(RespStatusEnum.TOO_MANY_RECEIVER.getCode(), context.getResponse().getStatus());
    }

    @Test
    void testProcessWithExactly100Receivers() {
        // 准备测试数据 - 正好100个接收者
        List<MessageParam> messageParamList = new ArrayList<>();
        MessageParam param = new MessageParam();
        
        StringBuilder receivers = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            if (i > 0) {
                receivers.append(",");
            }
            receivers.append("user").append(i);
        }
        param.setReceiver(receivers.toString());
        messageParamList.add(param);

        SendTaskModel sendTaskModel = SendTaskModel.builder()
                .messageTemplateId(1L)
                .messageParamList(messageParamList)
                .build();

        ProcessContext<SendTaskModel> context = ProcessContext.<SendTaskModel>builder()
                .code("TEST")
                .processModel(sendTaskModel)
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        // 执行测试
        sendPreCheckAction.process(context);

        // 验证结果 - 100个接收者应该通过校验
        assertFalse(context.getNeedBreak());
        assertEquals(1, sendTaskModel.getMessageParamList().size());
    }
}
