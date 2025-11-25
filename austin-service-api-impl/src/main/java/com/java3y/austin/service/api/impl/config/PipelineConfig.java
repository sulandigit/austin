package com.java3y.austin.service.api.impl.config;


import com.java3y.austin.common.pipeline.ProcessController;
import com.java3y.austin.common.pipeline.ProcessTemplate;
import com.java3y.austin.service.api.enums.BusinessCode;
import com.java3y.austin.service.api.impl.action.recall.RecallAssembleAction;
import com.java3y.austin.service.api.impl.action.recall.RecallMqAction;
import com.java3y.austin.service.api.impl.action.send.SendAfterCheckAction;
import com.java3y.austin.service.api.impl.action.send.SendAssembleAction;
import com.java3y.austin.service.api.impl.action.send.SendMqAction;
import com.java3y.austin.service.api.impl.action.send.SendPreCheckAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * api层的pipeline配置类
 * 该配置类用于定义和管理消息发送和撤回的业务流程模板
 * 通过责任链模式组织各个业务动作，实现流程的可配置化和可扩展性
 *
 * @author 3y
 */
@Configuration
public class PipelineConfig {

    /**
     * 发送前置参数校验动作
     * 负责校验发送请求的基础参数是否合法
     */
    @Autowired
    private SendPreCheckAction sendPreCheckAction;
    
    /**
     * 发送参数组装动作
     * 负责组装消息发送所需的完整参数
     */
    @Autowired
    private SendAssembleAction sendAssembleAction;
    
    /**
     * 发送后置参数校验动作
     * 负责校验组装后的参数是否完整且符合业务规则
     */
    @Autowired
    private SendAfterCheckAction sendAfterCheckAction;
    
    /**
     * 发送MQ动作
     * 负责将消息发送到消息队列
     */
    @Autowired
    private SendMqAction sendMqAction;

    /**
     * 撤回消息参数组装动作
     * 负责组装消息撤回所需的参数
     */
    @Autowired
    private RecallAssembleAction recallAssembleAction;
    
    /**
     * 撤回消息MQ动作
     * 负责将撤回指令发送到消息队列
     */
    @Autowired
    private RecallMqAction recallMqAction;


    /**
     * 普通发送执行流程模板
     * 定义消息发送的完整责任链流程，按顺序执行以下动作：
     * 1. 前置参数校验 - 校验请求的基础参数
     * 2. 组装参数 - 组装消息发送所需的完整数据
     * 3. 后置参数校验 - 校验组装后的参数完整性
     * 4. 发送消息至MQ - 将消息推送到消息队列
     *
     * @return ProcessTemplate 普通发送流程模板实例
     */
    @Bean("commonSendTemplate")
    public ProcessTemplate commonSendTemplate() {
        ProcessTemplate processTemplate = new ProcessTemplate();
        // 按顺序设置责任链中的各个处理动作
        processTemplate.setProcessList(Arrays.asList(sendPreCheckAction, sendAssembleAction,
                sendAfterCheckAction, sendMqAction));
        return processTemplate;
    }

    /**
     * 消息撤回执行流程模板
     * 定义消息撤回的责任链流程，按顺序执行以下动作：
     * 1. 组装参数 - 组装撤回消息所需的参数（如消息ID等）
     * 2. 发送MQ - 将撤回指令发送到消息队列
     *
     * @return ProcessTemplate 消息撤回流程模板实例
     */
    @Bean("recallMessageTemplate")
    public ProcessTemplate recallMessageTemplate() {
        ProcessTemplate processTemplate = new ProcessTemplate();
        // 按顺序设置责任链中的各个处理动作
        processTemplate.setProcessList(Arrays.asList(recallAssembleAction, recallMqAction));
        return processTemplate;
    }

    /**
     * pipeline流程控制器
     * 负责根据业务代码（BusinessCode）路由到对应的流程模板
     * 实现业务流程的统一管理和调度
     * 
     * 扩展说明：
     * 后续如需新增业务流程，只需：
     * 1. 在BusinessCode枚举中添加新的业务代码
     * 2. 创建对应的ProcessTemplate Bean
     * 3. 在templateConfig中添加映射关系即可
     *
     * @return ProcessController API层流程控制器实例
     */
    @Bean("apiProcessController")
    public ProcessController apiProcessController() {
        ProcessController processController = new ProcessController();
        // 初始化业务代码与流程模板的映射关系
        Map<String, ProcessTemplate> templateConfig = new HashMap<>(4);
        // 普通发送业务流程
        templateConfig.put(BusinessCode.COMMON_SEND.getCode(), commonSendTemplate());
        // 消息撤回业务流程
        templateConfig.put(BusinessCode.RECALL.getCode(), recallMessageTemplate());
        // 将配置设置到控制器中
        processController.setTemplateConfig(templateConfig);
        return processController;
    }

}
