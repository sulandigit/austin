package com.java3y.austin.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空值缓存对象
 * 用于缓存查询不存在的数据，防止缓存穿透
 *
 * @author 3y
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheNullValueHolder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据是否存在
     */
    private Boolean exists;

    /**
     * 缓存时间戳
     */
    private Long cacheTime;

    /**
     * 不存在原因
     */
    private String reason;

    /**
     * 创建表示数据不存在的空值对象
     *
     * @param reason 不存在原因
     * @return 空值对象
     */
    public static CacheNullValueHolder notFound(String reason) {
        return CacheNullValueHolder.builder()
                .exists(false)
                .cacheTime(System.currentTimeMillis())
                .reason(reason)
                .build();
    }

    /**
     * 判断是否为空值标记
     *
     * @return true表示是空值标记
     */
    public boolean isNullValue() {
        return Boolean.FALSE.equals(exists);
    }
}
