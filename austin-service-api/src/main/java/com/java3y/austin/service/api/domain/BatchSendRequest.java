package com.java3y.austin.service.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Request parameters for batch send API
 * 发送接口的参数
 * batch
 *
 * @author 3y
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchSendRequest {


    /**
     * Business type code to execute
     * 执行业务类型
     * Required, refer to BusinessCode enum
     * 必传,参考 BusinessCode枚举
     */
    private String code;


    /**
     * Message template ID
     * 消息模板Id
     * Required
     * 必传
     */
    private Long messageTemplateId;


    /**
     * Message related parameters
     * 消息相关的参数
     * Required
     * 必传
     */
    private List<MessageParam> messageParamList;


}
