package com.java3y.austin.support.utils;

import cn.hutool.core.collection.CollUtil;
import com.google.common.base.Throwables;
import com.java3y.austin.common.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author 3y
 * @date 2021/12/10
 * 对Redis的某些操作二次封装
 */
@Component
@Slf4j
public class RedisUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * mGet将结果封装为Map
     *
     * @param keys
     */
    public Map<String, String> mGet(List<String> keys) {
        HashMap<String, String> result = new HashMap<>(keys.size());
        try {
            List<String> value = redisTemplate.opsForValue().multiGet(keys);
            if (CollUtil.isNotEmpty(value)) {
                for (int i = 0; i < keys.size(); i++) {
                    if (Objects.nonNull(value.get(i))) {
                        result.put(keys.get(i), value.get(i));
                    }

                }
            }
        } catch (Exception e) {
            log.error("RedisUtils#mGet fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return result;
    }

    /**
     * hGetAll
     *
     * @param key
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("RedisUtils#hGetAll fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return new HashMap<>(2);
    }

    /**
     * lRange
     *
     * @param key
     */
    public List<String> lRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("RedisUtils#lRange fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return new ArrayList<>();
    }

    /**
     * pipeline 设置 key-value 并设置过期时间
     */
    public void pipelineSetEx(Map<String, String> keyValues, Long seconds) {
        try {
            redisTemplate.executePipelined((RedisCallback<String>) connection -> {
                for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                    connection.setEx(entry.getKey().getBytes(StandardCharsets.UTF_8), seconds,
                            entry.getValue().getBytes(StandardCharsets.UTF_8));
                }
                return null;
            });
        } catch (Exception e) {
            log.error("RedisUtils#pipelineSetEx fail! e:{}", Throwables.getStackTraceAsString(e));
        }
    }


    /**
     * lpush 方法 并指定 过期时间
     */
    public void lPush(String key, String value, Long seconds) {
        try {
            redisTemplate.executePipelined((RedisCallback<String>) connection -> {
                connection.lPush(key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8));
                connection.expire(key.getBytes(StandardCharsets.UTF_8), seconds);
                return null;
            });
        } catch (Exception e) {
            log.error("RedisUtils#lPush fail! e:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * lLen 方法
     */
    public Long lLen(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("RedisUtils#lLen fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return 0L;
    }

    /**
     * lPop 方法
     */
    public String lPop(String key) {
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("RedisUtils#lPop fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return "";
    }

    /**
     * pipeline 设置 key-value 并设置过期时间
     *
     * @param seconds 过期时间
     * @param delta   自增的步长
     */
    public void pipelineHashIncrByEx(Map<String, String> keyValues, Long seconds, Long delta) {
        try {
            redisTemplate.executePipelined((RedisCallback<String>) connection -> {
                for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                    connection.hIncrBy(entry.getKey().getBytes(StandardCharsets.UTF_8),
                            entry.getValue().getBytes(StandardCharsets.UTF_8),
                            delta);
                    connection.expire(entry.getKey().getBytes(StandardCharsets.UTF_8),
                            seconds);
                }
                return null;
            });
        } catch (Exception e) {
            log.error("redis pipelineSetEX fail! e:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 执行指定的lua脚本返回执行结果
     * --KEYS[1]: 限流 key
     * --ARGV[1]: 限流窗口
     * --ARGV[2]: 当前时间戳（作为score）
     * --ARGV[3]: 阈值
     * --ARGV[4]: score 对应的唯一value
     *
     * @param redisScript
     * @param keys
     * @param args
     * @return
     */
    public Boolean execLimitLua(RedisScript<Long> redisScript, List<String> keys, String... args) {

        // 可变参数转数组
        String[] argsArray = args != null ? args : new String[0];
        try {
            Long execute = redisTemplate.execute(redisScript, keys, (Object[]) argsArray);
            if (Objects.isNull(execute)) {
                return false;
            }
            return CommonConstant.TRUE.equals(execute.intValue());
        } catch (Exception e) {
            log.error("redis execLimitLua fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return false;
    }

    /**
     * 设置缓存，带随机过期时间（防止缓存雪崩）
     *
     * @param key            缓存key
     * @param value          缓存值
     * @param baseExpireTime 基准过期时间（秒）
     */
    public void setWithRandomExpire(String key, String value, Long baseExpireTime) {
        try {
            // 计算随机过期时间：基准时间 + [0, 20%] 的随机偏移
            long randomOffset = (long) (baseExpireTime * ThreadLocalRandom.current().nextDouble(0, 0.2));
            long actualExpireTime = baseExpireTime + randomOffset;
            
            redisTemplate.opsForValue().set(key, value, actualExpireTime, TimeUnit.SECONDS);
            log.debug("RedisUtils#setWithRandomExpire key:{}, baseExpire:{}s, actualExpire:{}s", 
                    key, baseExpireTime, actualExpireTime);
        } catch (Exception e) {
            log.error("RedisUtils#setWithRandomExpire fail! key:{}, e:{}", key, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 批量设置缓存，带随机过期时间（防止缓存雪崩）
     *
     * @param keyValues      key-value 映射
     * @param baseExpireTime 基准过期时间（秒）
     */
    public void pipelineSetWithRandomExpire(Map<String, String> keyValues, Long baseExpireTime) {
        try {
            redisTemplate.executePipelined((RedisCallback<String>) connection -> {
                for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                    // 每个key都有独立的随机过期时间
                    long randomOffset = (long) (baseExpireTime * ThreadLocalRandom.current().nextDouble(0, 0.2));
                    long actualExpireTime = baseExpireTime + randomOffset;
                    
                    connection.setEx(entry.getKey().getBytes(StandardCharsets.UTF_8), 
                            actualExpireTime,
                            entry.getValue().getBytes(StandardCharsets.UTF_8));
                }
                return null;
            });
            log.debug("RedisUtils#pipelineSetWithRandomExpire count:{}, baseExpire:{}s", 
                    keyValues.size(), baseExpireTime);
        } catch (Exception e) {
            log.error("RedisUtils#pipelineSetWithRandomExpire fail! e:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 获取缓存值
     *
     * @param key 缓存key
     * @return 缓存值
     */
    public String get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("RedisUtils#get fail! key:{}, e:{}", key, Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存key
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("RedisUtils#delete fail! key:{}, e:{}", key, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 判断key是否存在
     *
     * @param key 缓存key
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("RedisUtils#hasKey fail! key:{}, e:{}", key, Throwables.getStackTraceAsString(e));
            return false;
        }
    }


}
