package com.java3y.austin.support.utils;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 布隆过滤器管理器
 * 基于 Redis 实现分布式布隆过滤器，防止缓存穿透
 *
 * @author 3y
 */
@Slf4j
@Component
public class BloomFilterManager {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 布隆过滤器 Key 前缀
     */
    private static final String BLOOM_FILTER_PREFIX = "bloom:";

    /**
     * API Key 布隆过滤器
     */
    private static final String BLOOM_FILTER_APIKEY = BLOOM_FILTER_PREFIX + "apikey";

    /**
     * 消息模板布隆过滤器
     */
    private static final String BLOOM_FILTER_TEMPLATE = BLOOM_FILTER_PREFIX + "template";

    /**
     * 渠道账号布隆过滤器
     */
    private static final String BLOOM_FILTER_ACCOUNT = BLOOM_FILTER_PREFIX + "account";

    /**
     * 添加元素到布隆过滤器
     *
     * @param filterKey 布隆过滤器类型
     * @param value     要添加的值
     */
    public void add(String filterKey, String value) {
        try {
            redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                connection.commands().setBit(
                        (BLOOM_FILTER_PREFIX + filterKey).getBytes(StandardCharsets.UTF_8),
                        getHash(value, 0),
                        true
                );
                connection.commands().setBit(
                        (BLOOM_FILTER_PREFIX + filterKey).getBytes(StandardCharsets.UTF_8),
                        getHash(value, 1),
                        true
                );
                connection.commands().setBit(
                        (BLOOM_FILTER_PREFIX + filterKey).getBytes(StandardCharsets.UTF_8),
                        getHash(value, 2),
                        true
                );
                return true;
            });
        } catch (Exception e) {
            log.error("BloomFilterManager#add fail! filterKey:{}, value:{}, e:{}", 
                    filterKey, value, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 批量添加元素到布隆过滤器
     *
     * @param filterKey 布隆过滤器类型
     * @param values    要添加的值列表
     */
    public void batchAdd(String filterKey, List<String> values) {
        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (String value : values) {
                    connection.commands().setBit(
                            (BLOOM_FILTER_PREFIX + filterKey).getBytes(StandardCharsets.UTF_8),
                            getHash(value, 0),
                            true
                    );
                    connection.commands().setBit(
                            (BLOOM_FILTER_PREFIX + filterKey).getBytes(StandardCharsets.UTF_8),
                            getHash(value, 1),
                            true
                    );
                    connection.commands().setBit(
                            (BLOOM_FILTER_PREFIX + filterKey).getBytes(StandardCharsets.UTF_8),
                            getHash(value, 2),
                            true
                    );
                }
                return null;
            });
            log.info("BloomFilterManager#batchAdd success! filterKey:{}, count:{}", filterKey, values.size());
        } catch (Exception e) {
            log.error("BloomFilterManager#batchAdd fail! filterKey:{}, e:{}", 
                    filterKey, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 判断元素是否可能存在
     *
     * @param filterKey 布隆过滤器类型
     * @param value     要判断的值
     * @return true表示可能存在，false表示一定不存在
     */
    public boolean mightContain(String filterKey, String value) {
        try {
            List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                connection.commands().getBit(
                        (BLOOM_FILTER_PREFIX + filterKey).getBytes(StandardCharsets.UTF_8),
                        getHash(value, 0)
                );
                connection.commands().getBit(
                        (BLOOM_FILTER_PREFIX + filterKey).getBytes(StandardCharsets.UTF_8),
                        getHash(value, 1)
                );
                connection.commands().getBit(
                        (BLOOM_FILTER_PREFIX + filterKey).getBytes(StandardCharsets.UTF_8),
                        getHash(value, 2)
                );
                return null;
            });

            // 所有位都为 true 才表示可能存在
            for (Object result : results) {
                if (!Boolean.TRUE.equals(result)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("BloomFilterManager#mightContain fail! filterKey:{}, value:{}, e:{}", 
                    filterKey, value, Throwables.getStackTraceAsString(e));
            // 出现异常时，保守策略：返回true，让请求继续执行
            return true;
        }
    }

    /**
     * 删除布隆过滤器
     *
     * @param filterKey 布隆过滤器类型
     */
    public void delete(String filterKey) {
        try {
            redisTemplate.delete(BLOOM_FILTER_PREFIX + filterKey);
            log.info("BloomFilterManager#delete success! filterKey:{}", filterKey);
        } catch (Exception e) {
            log.error("BloomFilterManager#delete fail! filterKey:{}, e:{}", 
                    filterKey, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 计算哈希值
     * 使用简单的哈希算法，结合种子值生成不同的哈希
     *
     * @param value 要哈希的值
     * @param seed  种子值
     * @return 哈希位位置
     */
    private long getHash(String value, int seed) {
        long hash = 0;
        for (int i = 0; i < value.length(); i++) {
            hash = hash * 31 + value.charAt(i);
        }
        hash = hash * seed + seed;
        // 使用 100万个位，约 122KB 内存
        return Math.abs(hash % 1000000);
    }

    /**
     * 获取 API Key 布隆过滤器名称
     */
    public static String getApiKeyFilter() {
        return "apikey";
    }

    /**
     * 获取消息模板布隆过滤器名称
     */
    public static String getTemplateFilter() {
        return "template";
    }

    /**
     * 获取渠道账号布隆过滤器名称
     */
    public static String getAccountFilter() {
        return "account";
    }
}
