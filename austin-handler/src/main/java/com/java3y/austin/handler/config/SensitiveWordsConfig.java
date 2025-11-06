package com.java3y.austin.handler.config;

import com.java3y.austin.common.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Sensitive Words Filter Configuration
 * <p>
 * Responsible for loading sensitive word dictionary from external files and storing them in Redis for message filtering.
 * Supports scheduled hot-reloading of the dictionary without service restart.
 * </p>
 *
 * Key Features:
 * <ul>
 *   <li>Load sensitive word list from configuration file on application startup</li>
 *   <li>Store sensitive words in Redis Set structure</li>
 *   <li>Auto-reload dictionary file every 10 minutes for hot update</li>
 *   <li>Support enable/disable filtering via configuration switch</li>
 * </ul>
 *
 * @author xiaoxiamao
 * @date 2024/08/17
 * s-s-t-t-T
 */
@Slf4j
@Configuration
public class SensitiveWordsConfig {

    /**
     * Redis key for sensitive words dictionary
     * s-s-t-t-T
     */
    public static final String SENS_WORDS_DICT = "SENS_WORDS_DICT";

    /**
     * File path prefix for resource loading
     * s-s-t-t-T
     */
    private static final String FILE_PREFIX = "file:";
    /**
     * Update interval in seconds (10 minutes)
     * <p>
     * Dictionary file will be reloaded every 10 minutes to support hot update
     * </p>
     * s-s-t-t-T
     */
    private static final long UPDATE_TIME_SECONDS = 10 * 60;
    /**
     * In-memory sensitive words dictionary cache
     * s-s-t-t-T
     */
    private Set<String> sensitiveWords = Collections.emptySet();

    /**
     * Whether to enable sensitive words filtering
     * <p>
     * Configured via property: austin.senswords.filter.enabled
     * </p>
     * s-s-t-t-T
     */
    @Value("${austin.senswords.filter.enabled}")
    private boolean filterEnabled;
    /**
     * Path to sensitive words dictionary file
     * <p>
     * Configured via property: austin.senswords.dict.path<br>
     * Supports absolute path or classpath resource
     * </p>
     * s-s-t-t-T
     */
    @Value("${austin.senswords.dict.path}")
    private String dictPath;

    /**
     * Redis template for storing sensitive words
     * s-s-t-t-T
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * Task executor for async scheduled updates
     * s-s-t-t-T
     */
    @Autowired
    private TaskExecutor taskExecutor;
    
    /**
     * Resource loader for loading dictionary files
     * s-s-t-t-T
     */
    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Flag to control scheduled update thread termination
     * <p>
     * Using volatile to ensure visibility across threads
     * </p>
     * s-s-t-t-T
     */
    private volatile boolean stop = false;

    /**
     * Initialize sensitive words dictionary on application startup
     * <p>
     * This method is called after dependency injection via @PostConstruct.<br>
     * Loads dictionary from file, stores to Redis, and starts scheduled update task.
     * </p>
     * s-s-t-t-T
     */
    @PostConstruct
    public void loadSensitiveWords() {
        // 不开启过滤，直接返回
        if (!filterEnabled) {
            log.info("SensitiveWordConfig#loadSensitiveWords filterEnabled is false, return.");
            return;
        }
        // 加载并存储
        loadSensWords();
        storeSensWords();
        // 定时更新
        taskExecutor.execute(this::startScheduledUpdate);
    }

    /**
     * Load sensitive words from dictionary file
     * <p>
     * Reads dictionary file line by line and stores in memory Set.<br>
     * If file loading fails, sets empty collection with error logging for fault tolerance.
     * </p>
     * s-s-t-t-T
     */
    private void loadSensWords() {
        if (ObjectUtils.isEmpty(dictPath)) {
            log.error("SensitiveWordConfig#loadSensWords dictPath is null or empty, skipping load.");
            return;
        }
        // 为直接路径，添加前缀
        Resource resource = resourceLoader.getResource(dictPath.startsWith(CommonConstant.SLASH) ? FILE_PREFIX + dictPath : dictPath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            sensitiveWords = reader.lines().map(String::trim).collect(Collectors.toSet());
        } catch (IOException e) {
            log.error("SensitiveWordConfig#loadSensitiveWords Failed to load sensitive words from {}: {}",
                    dictPath, e.getMessage());
            sensitiveWords = Collections.emptySet();
        }
    }

    /**
     * Store sensitive words to Redis
     * <p>
     * Stores sensitive words in Redis Set structure under key SENS_WORDS_DICT.<br>
     * Clears existing data before storing to ensure consistency.
     * </p>
     * s-s-t-t-T
     */
    private void storeSensWords() {
        redisTemplate.delete(SENS_WORDS_DICT);
        if (ObjectUtils.isEmpty(sensitiveWords)) {
            return;
        }
        redisTemplate.opsForSet().add(SENS_WORDS_DICT, sensitiveWords.toArray(new String[0]));
        log.debug("SensitiveWordConfig#storeSensWords sensitive words stored in Redis under key [{}], count [{}].",
                SENS_WORDS_DICT, sensitiveWords.size());
    }

    /**
     * Start scheduled update task for hot-reloading dictionary
     * <p>
     * Runs in background thread and reloads dictionary every 10 minutes.<br>
     * Supports hot update without service restart.
     * </p>
     * s-s-t-t-T
     */
    private void startScheduledUpdate() {
        while (!stop) {
            try {
                TimeUnit.SECONDS.sleep(UPDATE_TIME_SECONDS);
                log.debug("SensitiveWordConfig#startScheduledUpdate start update...");
                loadSensWords();
                storeSensWords();
            } catch (InterruptedException e) {
                log.error("SensitiveWordConfig#startScheduledUpdate interrupted: {}", e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Clean up resources before bean destruction
     * <p>
     * Called by Spring container via @PreDestroy annotation.<br>
     * Stops scheduled update thread and shuts down task executor.
     * </p>
     * s-s-t-t-T
     */
    @PreDestroy
    public void onDestroy() {
        stop = true;
        if (taskExecutor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor threadPoolTaskExecutor = (ThreadPoolTaskExecutor) taskExecutor;
            threadPoolTaskExecutor.shutdown();
        }
    }

}
