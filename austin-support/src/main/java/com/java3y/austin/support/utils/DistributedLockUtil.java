package com.java3y.austin.support.utils;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类
 * 基于 Redis 实现分布式锁，防止缓存击穿
 *
 * @author 3y
 */
@Slf4j
@Component
public class DistributedLockUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 锁前缀
     */
    private static final String LOCK_PREFIX = "lock:cache:";

    /**
     * 锁默认过期时间（秒）
     */
    private static final long DEFAULT_EXPIRE_TIME = 10L;

    /**
     * 获取锁默认超时时间（秒）
     */
    private static final long DEFAULT_TIMEOUT = 3L;

    /**
     * 重试间隔（毫秒）
     */
    private static final long RETRY_INTERVAL = 50L;

    /**
     * 尝试获取分布式锁
     *
     * @param key 锁的key
     * @return 锁的唯一标识，获取失败返回null
     */
    public String tryLock(String key) {
        return tryLock(key, DEFAULT_EXPIRE_TIME, DEFAULT_TIMEOUT);
    }

    /**
     * 尝试获取分布式锁
     *
     * @param key        锁的key
     * @param expireTime 锁过期时间（秒）
     * @param timeout    获取锁超时时间（秒）
     * @return 锁的唯一标识，获取失败返回null
     */
    public String tryLock(String key, long expireTime, long timeout) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = UUID.randomUUID().toString();
        
        try {
            long start = System.currentTimeMillis();
            long end = start + timeout * 1000;

            // 在超时时间内重试获取锁
            while (System.currentTimeMillis() < end) {
                Boolean success = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.SECONDS);
                
                if (Boolean.TRUE.equals(success)) {
                    log.debug("DistributedLockUtil#tryLock success! key:{}, lockValue:{}", key, lockValue);
                    return lockValue;
                }

                // 未获取到锁，短暂休眠后重试
                Thread.sleep(RETRY_INTERVAL);
            }

            log.warn("DistributedLockUtil#tryLock timeout! key:{}, timeout:{}s", key, timeout);
            return null;
        } catch (Exception e) {
            log.error("DistributedLockUtil#tryLock fail! key:{}, e:{}", 
                    key, Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    /**
     * 释放分布式锁
     *
     * @param key       锁的key
     * @param lockValue 锁的唯一标识
     * @return 释放成功返回true
     */
    public boolean releaseLock(String key, String lockValue) {
        if (lockValue == null) {
            return false;
        }

        String lockKey = LOCK_PREFIX + key;
        try {
            // 使用Lua脚本保证原子性：只有当值匹配时才删除
            String luaScript = 
                    "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('del', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";

            Long result = redisTemplate.execute(
                    (org.springframework.data.redis.core.script.RedisCallback<Long>) connection -> 
                            connection.scriptingCommands().eval(
                                    luaScript.getBytes(),
                                    org.springframework.data.redis.connection.ReturnType.INTEGER,
                                    1,
                                    lockKey.getBytes(),
                                    lockValue.getBytes()
                            )
            );

            boolean success = result != null && result == 1L;
            if (success) {
                log.debug("DistributedLockUtil#releaseLock success! key:{}, lockValue:{}", key, lockValue);
            } else {
                log.warn("DistributedLockUtil#releaseLock fail! Lock not owned or expired. key:{}", key);
            }
            return success;
        } catch (Exception e) {
            log.error("DistributedLockUtil#releaseLock fail! key:{}, e:{}", 
                    key, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 执行加锁操作，自动释放锁
     *
     * @param key      锁的key
     * @param callback 加锁后执行的回调
     * @param <T>      返回值类型
     * @return 回调执行结果
     */
    public <T> T executeWithLock(String key, LockCallback<T> callback) {
        return executeWithLock(key, DEFAULT_EXPIRE_TIME, DEFAULT_TIMEOUT, callback);
    }

    /**
     * 执行加锁操作，自动释放锁
     *
     * @param key        锁的key
     * @param expireTime 锁过期时间（秒）
     * @param timeout    获取锁超时时间（秒）
     * @param callback   加锁后执行的回调
     * @param <T>        返回值类型
     * @return 回调执行结果
     */
    public <T> T executeWithLock(String key, long expireTime, long timeout, LockCallback<T> callback) {
        String lockValue = tryLock(key, expireTime, timeout);
        if (lockValue == null) {
            log.warn("DistributedLockUtil#executeWithLock fail to acquire lock! key:{}", key);
            return null;
        }

        try {
            return callback.execute();
        } catch (Exception e) {
            log.error("DistributedLockUtil#executeWithLock callback fail! key:{}, e:{}", 
                    key, Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e);
        } finally {
            releaseLock(key, lockValue);
        }
    }

    /**
     * 锁回调接口
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface LockCallback<T> {
        /**
         * 在持有锁的情况下执行的操作
         *
         * @return 执行结果
         */
        T execute();
    }
}
