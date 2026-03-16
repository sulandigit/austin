package com.java3y.austin.handler.utils;


import com.java3y.austin.common.domain.TaskInfo;
import com.java3y.austin.common.enums.ChannelType;
import com.java3y.austin.common.enums.EnumUtil;
import com.java3y.austin.common.enums.MessageType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * groupId 标识着每一个消费者组
 *
 * @author 3y
 */
public class GroupIdMappingUtils {

    private static final String GROUP_ID_SEPARATOR = ".";

    private GroupIdMappingUtils() {
    }

    /**
     * 获取所有的groupIds
     * (不同的渠道不同的消息类型拥有自己的groupId)
     */
    public static List<String> getAllGroupIds() {
        return Arrays.stream(ChannelType.values())
                .flatMap(channelType -> Arrays.stream(MessageType.values())
                        .map(messageType -> joinGroupId(channelType.getCodeEn(), messageType.getCodeEn())))
                .collect(Collectors.toList());
    }

    /**
     * 根据TaskInfo获取当前消息的groupId
     *
     * @param taskInfo 任务信息
     * @return groupId
     */
    public static String getGroupIdByTaskInfo(TaskInfo taskInfo) {
        ChannelType channelType = EnumUtil.getEnumByCode(taskInfo.getSendChannel(), ChannelType.class);
        MessageType messageType = EnumUtil.getEnumByCode(taskInfo.getMsgType(), MessageType.class);
        Objects.requireNonNull(channelType, "未找到对应的渠道类型, sendChannel=" + taskInfo.getSendChannel());
        Objects.requireNonNull(messageType, "未找到对应的消息类型, msgType=" + taskInfo.getMsgType());
        return joinGroupId(channelType.getCodeEn(), messageType.getCodeEn());
    }

    /**
     * 拼接groupId
     */
    private static String joinGroupId(String channelCodeEn, String msgCodeEn) {
        return channelCodeEn + GROUP_ID_SEPARATOR + msgCodeEn;
    }
}
