package com.java3y.austin.service.api.impl.service;

import cn.monitor4all.logRecord.annotation.OperationLog;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.java3y.austin.common.domain.SimpleTaskInfo;
import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.common.pipeline.ProcessContext;
import com.java3y.austin.common.pipeline.ProcessController;
import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.service.api.domain.BatchSendRequest;
import com.java3y.austin.service.api.domain.SendRequest;
import com.java3y.austin.service.api.domain.SendResponse;
import com.java3y.austin.service.api.impl.domain.SendTaskModel;
import com.java3y.austin.service.api.service.SendService;
import com.java3y.austin.support.constans.SentinelConstant;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 发送接口
 *
 * @author 3y
 */
@Service
public class SendServiceImpl implements SendService {

    @Autowired
    @Qualifier("apiProcessController")
    private ProcessController processController;

    @Override
    @OperationLog(bizType = "SendService#send", bizId = "#sendRequest.messageTemplateId", msg = "#sendRequest")
    @SentinelResource(value = SentinelConstant.RESOURCE_SEND_SINGLE, 
                      blockHandler = "sendBlockHandler",
                      fallback = "sendFallback")
    public SendResponse send(SendRequest sendRequest) {
        if (ObjectUtils.isEmpty(sendRequest)) {
            return new SendResponse(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), RespStatusEnum.CLIENT_BAD_PARAMETERS.getMsg(), null);
        }

        SendTaskModel sendTaskModel = SendTaskModel.builder()
                .messageTemplateId(sendRequest.getMessageTemplateId())
                .messageParamList(Collections.singletonList(sendRequest.getMessageParam()))
                .build();

        ProcessContext context = ProcessContext.builder()
                .code(sendRequest.getCode())
                .processModel(sendTaskModel)
                .needBreak(false)
                .response(BasicResultVO.success()).build();

        ProcessContext process = processController.process(context);

        return new SendResponse(process.getResponse().getStatus(), process.getResponse().getMsg(), (List<SimpleTaskInfo>) process.getResponse().getData());
    }

    @Override
    @OperationLog(bizType = "SendService#batchSend", bizId = "#batchSendRequest.messageTemplateId", msg = "#batchSendRequest")
    @SentinelResource(value = SentinelConstant.RESOURCE_SEND_BATCH,
                      blockHandler = "batchSendBlockHandler",
                      fallback = "batchSendFallback")
    public SendResponse batchSend(BatchSendRequest batchSendRequest) {
        if (ObjectUtils.isEmpty(batchSendRequest)) {
            return new SendResponse(RespStatusEnum.CLIENT_BAD_PARAMETERS.getCode(), RespStatusEnum.CLIENT_BAD_PARAMETERS.getMsg(), null);
        }

        SendTaskModel sendTaskModel = SendTaskModel.builder()
                .messageTemplateId(batchSendRequest.getMessageTemplateId())
                .messageParamList(batchSendRequest.getMessageParamList())
                .build();

        ProcessContext context = ProcessContext.builder()
                .code(batchSendRequest.getCode())
                .processModel(sendTaskModel)
                .needBreak(false)
                .response(BasicResultVO.success()).build();

        ProcessContext process = processController.process(context);

        return new SendResponse(process.getResponse().getStatus(), process.getResponse().getMsg(), (List<SimpleTaskInfo>) process.getResponse().getData());
    }

    /**
     * send 方法的 Block Handler（限流/熔断时调用）
     */
    public SendResponse sendBlockHandler(SendRequest sendRequest, com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return new SendResponse(RespStatusEnum.SERVICE_ERROR.getCode(), 
                                SentinelConstant.FALLBACK_MSG_FLOW_CONTROL, 
                                null);
    }

    /**
     * send 方法的 Fallback（异常时调用）
     */
    public SendResponse sendFallback(SendRequest sendRequest, Throwable ex) {
        return new SendResponse(RespStatusEnum.SERVICE_ERROR.getCode(), 
                                "Send service error: " + ex.getMessage(), 
                                null);
    }

    /**
     * batchSend 方法的 Block Handler（限流/熔断时调用）
     */
    public SendResponse batchSendBlockHandler(BatchSendRequest batchSendRequest, com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return new SendResponse(RespStatusEnum.SERVICE_ERROR.getCode(), 
                                SentinelConstant.FALLBACK_MSG_FLOW_CONTROL, 
                                null);
    }

    /**
     * batchSend 方法的 Fallback（异常时调用）
     */
    public SendResponse batchSendFallback(BatchSendRequest batchSendRequest, Throwable ex) {
        return new SendResponse(RespStatusEnum.SERVICE_ERROR.getCode(), 
                                "Batch send service error: " + ex.getMessage(), 
                                null);
    }

}
