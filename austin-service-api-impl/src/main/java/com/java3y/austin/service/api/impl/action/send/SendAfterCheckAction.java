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
import java.util.stream.Collectors;

/**
 * 发送任务后置参数检查处理器
 * <p>
 * 在发送任务创建后，对接收者信息进行验证和过滤
 * 主要功能：
 * 1. 验证手机号格式是否合法（符合中国大陆手机号规则）
 * 2. 验证邮箱地址格式是否合法
 * 3. 过滤掉不合法的接收者，避免无效发送
 * 
 * @author 3y
 */
@Slf4j
@Service
public class SendAfterCheckAction implements BusinessProcess<SendTaskModel> {

    /**
     * 手机号正则表达式
     * 支持的号段：130-139, 145/147/149, 150-153/155-159, 166, 170-179, 180-189, 191/198/199
     */
    public static final String PHONE_REGEX_EXP = "^((13[0-9])|(14[5,7,9])|(15[0-3,5-9])|(166)|(17[0-9])|(18[0-9])|(19[1,8,9]))\\d{8}$";
    
    /**
     * 邮箱地址正则表达式
     * 格式：用户名@域名.顶级域名（顶级域名至少2位字符）
     */
    public static final String EMAIL_REGEX_EXP = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    
    /**
     * 不同ID类型对应的正则表达式映射
     * key: IdType类型编码（手机号、邮箱等）
     * value: 对应的正则表达式
     */
    protected static final Map<Integer, String> CHANNEL_REGEX_EXP;

    static {
        // 初始化ID类型与正则表达式的映射关系
        Map<Integer, String> tempMap = new HashMap<>();
        // 手机号类型 -> 手机号正则
        tempMap.put(IdType.PHONE.getCode(), PHONE_REGEX_EXP);
        // 邮箱类型 -> 邮箱正则
        tempMap.put(IdType.EMAIL.getCode(), EMAIL_REGEX_EXP);
        // 初始化为不可变集合，避免被恶意修改
        CHANNEL_REGEX_EXP = Collections.unmodifiableMap(tempMap);
    }


    /**
     * 处理发送任务的后置检查
     * <p>
     * 执行流程：
     * 1. 从上下文中获取发送任务模型
     * 2. 过滤掉接收者中不合法的手机号或邮箱
     * 3. 如果过滤后没有有效的接收者，设置错误响应并中断流程
     *
     * @param context 流程上下文，包含发送任务模型和流程控制信息
     */
    @Override
    public void process(ProcessContext<SendTaskModel> context) {
        // 获取发送任务模型
        SendTaskModel sendTaskModel = context.getProcessModel();
        // 获取任务列表
        List<TaskInfo> taskInfo = sendTaskModel.getTaskInfo();

        // 过滤掉不合法的手机号、邮件
        filterIllegalReceiver(taskInfo);
        
        // 如果过滤后任务列表为空，说明没有有效的接收者
        if (CollUtil.isEmpty(taskInfo)) {
            // 设置需要中断流程，并返回错误响应
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.CLIENT_BAD_PARAMETERS, "手机号或邮箱不合法, 无有效的发送任务"));
        }

    }

    /**
     * 过滤不合法的接收者
     * <p>
     * 根据任务的ID类型（手机号或邮箱），选择对应的正则表达式进行验证
     * 如果指定类型是手机号，检测输入手机号是否合法
     * 如果指定类型是邮件，检测输入邮件是否合法
     *
     * @param taskInfo 任务信息列表，会被直接修改（移除不合法的接收者）
     */
    private void filterIllegalReceiver(List<TaskInfo> taskInfo) {
        // 获取第一个任务的ID类型（手机号、邮箱等）
        Integer idType = CollUtil.getFirst(taskInfo.iterator()).getIdType();
        // 根据ID类型获取对应的正则表达式，并执行过滤
        filter(taskInfo, CHANNEL_REGEX_EXP.get(idType));
    }

    /**
     * 利用正则表达式过滤掉不合法的接收者
     * <p>
     * 执行逻辑：
     * 1. 遍历每个任务的接收者列表
     * 2. 使用正则表达式验证每个接收者是否合法
     * 3. 移除不合法的接收者，并记录日志
     * 4. 如果某个任务的所有接收者都不合法，则移除整个任务
     *
     * @param taskInfo 任务信息列表，会被直接修改
     * @param regexExp 用于验证接收者的正则表达式
     */
    private void filter(List<TaskInfo> taskInfo, String regexExp) {
        // 使用迭代器遍历，以便在遍历过程中安全删除元素
        Iterator<TaskInfo> iterator = taskInfo.iterator();
        while (iterator.hasNext()) {
            TaskInfo task = iterator.next();
            
            // 筛选出所有不匹配正则表达式的接收者（不合法的接收者）
            Set<String> illegalPhone = task.getReceiver().stream()
                    .filter(phone -> !ReUtil.isMatch(regexExp, phone))
                    .collect(Collectors.toSet());

            // 如果存在不合法的接收者
            if (CollUtil.isNotEmpty(illegalPhone)) {
                // 从接收者列表中移除所有不合法的接收者
                task.getReceiver().removeAll(illegalPhone);
                // 记录错误日志，包含消息模板ID和不合法的接收者列表
                log.error("messageTemplateId:{} find illegal receiver!{}", task.getMessageTemplateId(), JSON.toJSONString(illegalPhone));
            }
            
            // 如果该任务的接收者列表为空（所有接收者都不合法），则移除该任务
            if (CollUtil.isEmpty(task.getReceiver())) {
                iterator.remove();
            }
        }
    }

}
