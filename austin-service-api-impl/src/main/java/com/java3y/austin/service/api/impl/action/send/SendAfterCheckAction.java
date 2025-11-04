package com.java3y.austin.service.api.impl.action.send;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.java3y.austin.common.domain.TaskInfo;
import com.java3y.austin.common.enums.IdType;
import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.common.pipeline.BusinessProcess;
import com.java3y.austin.common.pipeline.ProcessContext;
import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.service.api.impl.domain.SendTaskModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 3y
 * <p>
 * 后置参数检查
 */
@Slf4j
@Service
public class SendAfterCheckAction implements BusinessProcess<SendTaskModel> {

    // 优化：使用Pattern预编译正则表达式，提高性能
    private static final Pattern PHONE_PATTERN = Pattern.compile("^((13[0-9])|(14[5,7,9])|(15[0-3,5-9])|(166)|(17[0-9])|(18[0-9])|(19[1,8,9]))\\d{8}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    public static final String PHONE_REGEX_EXP = PHONE_PATTERN.pattern();
    public static final String EMAIL_REGEX_EXP = EMAIL_PATTERN.pattern();
    
    /**
     * 邮件和手机号正则
     */
    protected static final Map<Integer, Pattern> CHANNEL_PATTERN_MAP;
    protected static final Map<Integer, String> CHANNEL_REGEX_EXP;

    static {
        Map<Integer, Pattern> patternMap = new HashMap<>();
        patternMap.put(IdType.PHONE.getCode(), PHONE_PATTERN);
        patternMap.put(IdType.EMAIL.getCode(), EMAIL_PATTERN);
        CHANNEL_PATTERN_MAP = Collections.unmodifiableMap(patternMap);
        
        Map<Integer, String> tempMap = new HashMap<>();
        tempMap.put(IdType.PHONE.getCode(), PHONE_REGEX_EXP);
        tempMap.put(IdType.EMAIL.getCode(), EMAIL_REGEX_EXP);
        // 初始化为不可变集合，避免被恶意修改
        CHANNEL_REGEX_EXP = Collections.unmodifiableMap(tempMap);
    }


    @Override
    public void process(ProcessContext<SendTaskModel> context) {
        SendTaskModel sendTaskModel = context.getProcessModel();
        List<TaskInfo> taskInfo = sendTaskModel.getTaskInfo();

        // 过滤掉不合法的手机号、邮件
        filterIllegalReceiver(taskInfo);
        if (CollUtil.isEmpty(taskInfo)) {
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.CLIENT_BAD_PARAMETERS, "手机号或邮箱不合法, 无有效的发送任务"));
        }

    }

    /**
     * 如果指定类型是手机号，检测输入手机号是否合法
     * 如果指定类型是邮件，检测输入邮件是否合法
     *
     * @param taskInfo
     */
    private void filterIllegalReceiver(List<TaskInfo> taskInfo) {
        Integer idType = CollUtil.getFirst(taskInfo.iterator()).getIdType();
        // 优化：使用预编译的Pattern对象
        Pattern pattern = CHANNEL_PATTERN_MAP.get(idType);
        if (pattern != null) {
            filter(taskInfo, pattern);
        }
    }

    /**
     * 利用正则过滤掉不合法的接收者
     *
     * @param taskInfo
     * @param pattern
     */
    private void filter(List<TaskInfo> taskInfo, Pattern pattern) {
        Iterator<TaskInfo> iterator = taskInfo.iterator();
        while (iterator.hasNext()) {
            TaskInfo task = iterator.next();
            // 优化：使用预编译的Pattern，避免重复编译
            Set<String> illegalPhone = task.getReceiver().stream()
                    .filter(phone -> !pattern.matcher(phone).matches())
                    .collect(Collectors.toSet());

            if (CollUtil.isNotEmpty(illegalPhone)) {
                task.getReceiver().removeAll(illegalPhone);
                log.error("messageTemplateId:{} find illegal receiver!{}", task.getMessageTemplateId(), JSON.toJSONString(illegalPhone));
            }
            if (CollUtil.isEmpty(task.getReceiver())) {
                iterator.remove();
            }
        }
    }

}
