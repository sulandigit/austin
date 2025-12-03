package com.java3y.austin.common.pipeline;

import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.common.vo.BasicResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProcessController 单元测试
 *
 * @author austin
 */
class ProcessControllerTest {

    private ProcessController processController;
    private BusinessProcess<TestProcessModel> mockProcess1;
    private BusinessProcess<TestProcessModel> mockProcess2;

    @BeforeEach
    void setUp() {
        processController = new ProcessController();
        mockProcess1 = mock(BusinessProcess.class);
        mockProcess2 = mock(BusinessProcess.class);
    }

    @Test
    void testProcessSuccess() {
        // 准备测试数据
        TestProcessModel model = new TestProcessModel();
        ProcessContext<TestProcessModel> context = ProcessContext.<TestProcessModel>builder()
                .code("TEST_CODE")
                .processModel(model)
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        // 设置模板配置
        ProcessTemplate template = new ProcessTemplate();
        template.setProcessList(Arrays.asList(mockProcess1, mockProcess2));
        Map<String, ProcessTemplate> templateConfig = new HashMap<>();
        templateConfig.put("TEST_CODE", template);
        processController.setTemplateConfig(templateConfig);

        // 执行测试
        ProcessContext result = processController.process(context);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.getNeedBreak());
        assertEquals(RespStatusEnum.SUCCESS.getCode(), result.getResponse().getStatus());
        verify(mockProcess1, times(1)).process(any());
        verify(mockProcess2, times(1)).process(any());
    }

    @Test
    void testProcessWithBreak() {
        // 准备测试数据
        TestProcessModel model = new TestProcessModel();
        ProcessContext<TestProcessModel> context = ProcessContext.<TestProcessModel>builder()
                .code("TEST_CODE")
                .processModel(model)
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        // 模拟第一个处理器设置中断
        doAnswer(invocation -> {
            ProcessContext ctx = invocation.getArgument(0);
            ctx.setNeedBreak(true);
            return null;
        }).when(mockProcess1).process(any());

        // 设置模板配置
        ProcessTemplate template = new ProcessTemplate();
        template.setProcessList(Arrays.asList(mockProcess1, mockProcess2));
        Map<String, ProcessTemplate> templateConfig = new HashMap<>();
        templateConfig.put("TEST_CODE", template);
        processController.setTemplateConfig(templateConfig);

        // 执行测试
        ProcessContext result = processController.process(context);

        // 验证结果 - 第二个处理器不应被执行
        assertNotNull(result);
        assertTrue(result.getNeedBreak());
        verify(mockProcess1, times(1)).process(any());
        verify(mockProcess2, never()).process(any());
    }

    @Test
    void testProcessWithNullContext() {
        // 执行测试
        ProcessContext result = processController.process(null);

        // 验证结果
        assertNotNull(result);
        assertEquals(RespStatusEnum.CONTEXT_IS_NULL.getCode(), result.getResponse().getStatus());
    }

    @Test
    void testProcessWithNullBusinessCode() {
        // 准备测试数据
        ProcessContext<TestProcessModel> context = ProcessContext.<TestProcessModel>builder()
                .code(null)
                .processModel(new TestProcessModel())
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        // 执行测试
        ProcessContext result = processController.process(context);

        // 验证结果
        assertNotNull(result);
        assertEquals(RespStatusEnum.BUSINESS_CODE_IS_NULL.getCode(), result.getResponse().getStatus());
    }

    @Test
    void testProcessWithNullTemplate() {
        // 准备测试数据
        ProcessContext<TestProcessModel> context = ProcessContext.<TestProcessModel>builder()
                .code("NON_EXISTENT_CODE")
                .processModel(new TestProcessModel())
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        processController.setTemplateConfig(new HashMap<>());

        // 执行测试
        ProcessContext result = processController.process(context);

        // 验证结果
        assertNotNull(result);
        assertEquals(RespStatusEnum.PROCESS_TEMPLATE_IS_NULL.getCode(), result.getResponse().getStatus());
    }

    @Test
    void testProcessWithEmptyProcessList() {
        // 准备测试数据
        ProcessContext<TestProcessModel> context = ProcessContext.<TestProcessModel>builder()
                .code("TEST_CODE")
                .processModel(new TestProcessModel())
                .needBreak(false)
                .response(BasicResultVO.success())
                .build();

        // 设置空的处理器列表
        ProcessTemplate template = new ProcessTemplate();
        template.setProcessList(new ArrayList<>());
        Map<String, ProcessTemplate> templateConfig = new HashMap<>();
        templateConfig.put("TEST_CODE", template);
        processController.setTemplateConfig(templateConfig);

        // 执行测试
        ProcessContext result = processController.process(context);

        // 验证结果
        assertNotNull(result);
        assertEquals(RespStatusEnum.PROCESS_LIST_IS_NULL.getCode(), result.getResponse().getStatus());
    }

    // 测试用的ProcessModel实现
    static class TestProcessModel implements ProcessModel {
        private String data;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
