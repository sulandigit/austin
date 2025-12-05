package com.java3y.austin.service.api.impl.action.send;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.java3y.austin.common.enums.AnchorState;
import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.common.pipeline.BusinessProcess;
import com.java3y.austin.common.pipeline.ProcessContext;
import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.service.api.domain.MessageParam;
import com.java3y.austin.service.api.impl.domain.SendTaskModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 幂等性检查Action
 * 在所有业务校验之前执行，防止重复请求
 *
 * @author austin
 */
@Slf4j
@Service
public class IdempotencyAction implements BusinessProcess<SendTaskModel> {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 幂等性开关
     */
    @Value("${austin.idempotency.enabled:true}")
    private Boolean idempotencyEnabled;

    /**
     * 幂等时间窗口（秒）
     */
    @Value("${austin.idempotency.ttl:600}")
    private Long idempotencyTtl;

    /**
     * Redis Key前缀
     */
    @Value("${austin.idempotency.key.prefix:Austin:Idempotency:}")
    private String keyPrefix;

    @Override
    public void process(ProcessContext<SendTaskModel> context) {
        // 如果幂等性开关关闭，直接跳过
        if (!idempotencyEnabled) {
            log.debug("IdempotencyAction is disabled, skip idempotency check");
            return;
        }

        SendTaskModel sendTaskModel = context.getProcessModel();
        
        // 基本参数校验
        if (Objects.isNull(sendTaskModel) 
                || Objects.isNull(sendTaskModel.getMessageTemplateId()) 
                || CollUtil.isEmpty(sendTaskModel.getMessageParamList())) {
            // 参数为空，跳过幂等检查（由后续的SendPreCheckAction处理）
            return;
        }

        try {
            // 生成请求指纹
            String requestFingerprint = generateRequestFingerprint(sendTaskModel);
            
            // 构建Redis Key
            String redisKey = keyPrefix + requestFingerprint;
            
            // 尝试设置Redis键（SET NX）
            Boolean setResult = stringRedisTemplate.opsForValue()
                    .setIfAbsent(redisKey, String.valueOf(System.currentTimeMillis()), idempotencyTtl, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(setResult)) {
                // 首次请求，记录日志
                log.debug("IdempotencyAction: First request, fingerprint={}, templateId={}, receiverCount={}", 
                        requestFingerprint, 
                        sendTaskModel.getMessageTemplateId(),
                        sendTaskModel.getMessageParamList().size());
            } else {
                // 重复请求，幂等拦截
                log.info("IdempotencyAction: Duplicate request intercepted, fingerprint={}, templateId={}, request={}", 
                        requestFingerprint, 
                        sendTaskModel.getMessageTemplateId(),
                        JSON.toJSONString(sendTaskModel));
                
                // 设置needBreak标志，终止后续流程
                context.setNeedBreak(true)
                        .setResponse(BasicResultVO.fail(RespStatusEnum.IDEMPOTENT));
            }
        } catch (Exception e) {
            // Redis异常，降级策略：记录告警日志，放行请求
            log.error("IdempotencyAction: Redis error, fallback to allow request. templateId={}, error={}", 
                    sendTaskModel.getMessageTemplateId(), 
                    Throwables.getStackTraceAsString(e));
            // 不设置needBreak，继续后续流程
        }
    }

    /**
     * 生成请求指纹
     * 优先使用requestId，不存在则使用MD5(请求参数)
     *
     * @param sendTaskModel 发送任务模型
     * @return 请求指纹
     */
    private String generateRequestFingerprint(SendTaskModel sendTaskModel) {
        List<MessageParam> messageParamList = sendTaskModel.getMessageParamList();
        
        // 模式一：优先使用客户端传入的requestId
        if (CollUtil.isNotEmpty(messageParamList)) {
            MessageParam firstParam = messageParamList.get(0);
            if (CharSequenceUtil.isNotBlank(firstParam.getRequestId())) {
                return firstParam.getRequestId();
            }
        }
        
        // 模式二：基于请求参数生成MD5
        return generateMd5Fingerprint(sendTaskModel);
    }

    /**
     * 基于请求参数生成MD5指纹
     * 计算因子：messageTemplateId + receiver列表（排序后） + variables（JSON序列化）
     *
     * @param sendTaskModel 发送任务模型
     * @return MD5指纹
     */
    private String generateMd5Fingerprint(SendTaskModel sendTaskModel) {
        StringBuilder sb = new StringBuilder();
        
        // 1. 添加模板ID
        sb.append(sendTaskModel.getMessageTemplateId());
        
        // 2. 添加receiver列表（排序后）
        List<MessageParam> messageParamList = sendTaskModel.getMessageParamList();
        if (CollUtil.isNotEmpty(messageParamList)) {
            List<String> receivers = messageParamList.stream()
                    .map(MessageParam::getReceiver)
                    .filter(CharSequenceUtil::isNotBlank)
                    .sorted()
                    .collect(Collectors.toList());
            sb.append(String.join(",", receivers));
            
            // 3. 添加variables（JSON序列化）
            messageParamList.stream()
                    .filter(param -> Objects.nonNull(param.getVariables()))
                    .forEach(param -> sb.append(JSON.toJSONString(param.getVariables())));
        }
        
        // 生成MD5
        return DigestUtil.md5Hex(sb.toString());
    }
}
