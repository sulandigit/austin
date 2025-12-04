package com.java3y.austin.support.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 应用信息实体类
 * 用于签名验证的应用管理
 *
 * @author austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Accessors(chain = true)
public class AppInfo implements Serializable {

    /**
     * 应用ID（唯一标识）
     */
    @Id
    private String appId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 签名密钥（加密存储）
     */
    private String appSecret;

    /**
     * 应用状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 签名算法版本
     */
    private String signVersion;

    /**
     * 允许访问的IP列表（逗号分隔，可选）
     */
    private String allowedIps;

    /**
     * 是否强制启用签名验证：0-不启用 1-启用
     */
    private Integer signatureEnabled;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建时间（秒级时间戳）
     */
    private Integer created;

    /**
     * 更新时间（秒级时间戳）
     */
    private Integer updated;

    /**
     * 是否删除：0-未删除 1-已删除
     */
    private Integer isDeleted;

}
