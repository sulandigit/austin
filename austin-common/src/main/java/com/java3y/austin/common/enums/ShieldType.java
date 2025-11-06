package com.java3y.austin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Shield type
 * 屏蔽类型
 *
 * @author 3y
 */
@Getter
@ToString
@AllArgsConstructor
public enum ShieldType implements PowerfulEnum {


    /**
     * Template set to no night shield
     * 模板设置为夜间不屏蔽
     */
    NIGHT_NO_SHIELD(10, "夜间不屏蔽"),
    /**
     * Template set to night shield -- Messages received at night will be filtered out
     * 模板设置为夜间屏蔽  -- 凌晨接受到的消息会过滤掉
     */
    NIGHT_SHIELD(20, "夜间屏蔽"),
    /**
     * Template set to night shield (send at 9 AM next day) -- Messages received at night will be sent next day
     * 模板设置为夜间屏蔽(次日早上9点发送)  -- 凌晨接受到的消息会次日发送
     */
    NIGHT_SHIELD_BUT_NEXT_DAY_SEND(30, "夜间屏蔽(次日早上9点发送)");

    private final Integer code;
    private final String description;
}
