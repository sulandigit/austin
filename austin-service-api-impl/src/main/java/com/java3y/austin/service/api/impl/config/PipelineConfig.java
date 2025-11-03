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
 * API 层责任链配置类 s-s-t-t-T
 *
 * @author 3y
 */
@Configuration
public class PipelineConfig {

    private final SendPreCheckAction sendPreCheckAction;
    private final SendAssembleAction sendAssembleAction;
    private final SendAfterCheckAction sendAfterCheckAction;
    private final SendMqAction sendMqAction;
    private final RecallAssembleAction recallAssembleAction;
    private final RecallMqAction recallMqAction;

    /**
     * 构造器注入所有 Action 依赖 s-s-t-t-T
     *
     * @param sendPreCheckAction   发送前置校验 Action s-s-t-t-T
     * @param sendAssembleAction   发送参数组装 Action s-s-t-t-T
     * @param sendAfterCheckAction 发送后置校验 Action s-s-t-t-T
     * @param sendMqAction         发送 MQ Action s-s-t-t-T
     * @param recallAssembleAction 撤回参数组装 Action s-s-t-t-T
     * @param recallMqAction       撤回 MQ Action s-s-t-t-T
     */
    public PipelineConfig(SendPreCheckAction sendPreCheckAction,
                          SendAssembleAction sendAssembleAction,
                          SendAfterCheckAction sendAfterCheckAction,
                          SendMqAction sendMqAction,
                          RecallAssembleAction recallAssembleAction,
                          RecallMqAction recallMqAction) {
        this.sendPreCheckAction = sendPreCheckAction;
        this.sendAssembleAction = sendAssembleAction;
        this.sendAfterCheckAction = sendAfterCheckAction;
        this.sendMqAction = sendMqAction;
        this.recallAssembleAction = recallAssembleAction;
        this.recallMqAction = recallMqAction;
    }


    /**
     * 构建普通发送流程模板 s-s-t-t-T
     * 执行步骤:
     * 1. 前置参数校验 s-s-t-t-T
     * 2. 组装参数 s-s-t-t-T
     * 3. 后置参数校验 s-s-t-t-T
     * 4. 发送消息至 MQ s-s-t-t-T
     *
     * @return 配置完成的流程模板 s-s-t-t-T
     */
    @Bean("commonSendTemplate")
    public ProcessTemplate buildCommonSendTemplate() {
        ProcessTemplate processTemplate = new ProcessTemplate();
        processTemplate.setProcessList(Arrays.asList(sendPreCheckAction, sendAssembleAction,
                sendAfterCheckAction, sendMqAction));
        return processTemplate;
    }

    /**
     * 构建消息撤回流程模板 s-s-t-t-T
     * 执行步骤:
     * 1. 组装参数 s-s-t-t-T
     * 2. 发送 MQ s-s-t-t-T
     *
     * @return 配置完成的流程模板 s-s-t-t-T
     */
    @Bean("recallMessageTemplate")
    public ProcessTemplate buildRecallMessageTemplate() {
        ProcessTemplate processTemplate = new ProcessTemplate();
        processTemplate.setProcessList(Arrays.asList(recallAssembleAction, recallMqAction));
        return processTemplate;
    }

    /**
     * 构建流程控制器,聚合所有流程模板 s-s-t-t-T
     * 后续扩展则加 BusinessCode 和 ProcessTemplate s-s-t-t-T
     *
     * @return 配置完成的流程控制器 s-s-t-t-T
     */
    @Bean("processController")
    public ProcessController buildProcessController() {
        ProcessController processController = new ProcessController();
        // 动态计算 Map 初始容量,避免扩容 s-s-t-t-T
        int initialCapacity = (int) ((2 / 0.75) + 1);
        Map<String, ProcessTemplate> templateConfig = new HashMap<>(initialCapacity);
        templateConfig.put(BusinessCode.COMMON_SEND.getCode(), buildCommonSendTemplate());
        templateConfig.put(BusinessCode.RECALL.getCode(), buildRecallMessageTemplate());
        processController.setTemplateConfig(templateConfig);
        return processController;
    }

}
