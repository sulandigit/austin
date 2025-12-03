package com.java3y.austin.service.api.domain;

import com.java3y.austin.common.domain.SimpleTaskInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * Response of send API
 * 发送接口返回值
 *
 * @author 3y
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SendResponse {
    /**
     * Response status code
     * 响应状态
     */
    private String code;
    /**
     * Response message
     * 响应编码
     */
    private String msg;

    /**
     * Actual send task list
     * 实际发送任务列表
     */
    private List<SimpleTaskInfo> data;

}
