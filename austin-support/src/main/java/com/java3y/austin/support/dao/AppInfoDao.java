package com.java3y.austin.support.dao;

import com.java3y.austin.support.domain.AppInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 应用信息DAO
 * 用于管理签名验证的应用信息
 *
 * @author austin
 */
public interface AppInfoDao extends JpaRepository<AppInfo, String> {

    /**
     * 根据appId查询应用信息（排除已删除的记录）
     *
     * @param appId     应用ID
     * @param isDeleted 是否删除标识：0-未删除
     * @return 应用信息
     */
    Optional<AppInfo> findByAppIdAndIsDeleted(String appId, Integer isDeleted);

    /**
     * 根据appId和状态查询应用信息（排除已删除的记录）
     *
     * @param appId     应用ID
     * @param status    状态：1-启用
     * @param isDeleted 是否删除标识：0-未删除
     * @return 应用信息
     */
    Optional<AppInfo> findByAppIdAndStatusAndIsDeleted(String appId, Integer status, Integer isDeleted);

}
