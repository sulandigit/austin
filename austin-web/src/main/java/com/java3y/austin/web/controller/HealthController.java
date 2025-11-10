package com.java3y.austin.web.controller;


import com.google.common.base.Throwables;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 健康检测
 *
 * @author 3y
 */
@Slf4j
@RestController
@Api("健康检测")
public class HealthController {

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Autowired(required = false)
    private KafkaAdmin kafkaAdmin;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Value("${austin.mq.pipeline:eventBus}")
    private String mqPipeline;

    /**
     * 简单健康检测
     *
     * @return 健康状态
     */
    @GetMapping("/")
    @ApiOperation("/健康检测")
    public String health() {
        return "success";
    }

    /**
     * 详细健康检测
     *
     * @param detailed 是否返回详细信息
     * @return 健康检测详细信息
     */
    @GetMapping("/health")
    @ApiOperation("/详细健康检测")
    public Map<String, Object> detailedHealth(@RequestParam(defaultValue = "true") Boolean detailed) {
        Map<String, Object> healthInfo = new HashMap<>();
        
        try {
            // 基本信息
            healthInfo.put("status", "UP");
            healthInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            healthInfo.put("application", "austin");
            
            if (detailed) {
                // JVM 信息
                healthInfo.put("jvm", getJvmInfo());
                
                // 系统资源信息
                healthInfo.put("system", getSystemInfo());
                
                // 组件健康检查
                Map<String, Object> components = new HashMap<>();
                components.put("database", checkDatabase());
                components.put("redis", checkRedis());
                components.put("messageQueue", checkMessageQueue());
                healthInfo.put("components", components);
                healthInfo.put("mqPipeline", mqPipeline);
            }
            
            return healthInfo;
        } catch (Exception e) {
            log.error("HealthController#detailedHealth error: {}", Throwables.getStackTraceAsString(e));
            healthInfo.put("status", "DOWN");
            healthInfo.put("error", e.getMessage());
            return healthInfo;
        }
    }

    /**
     * 获取JVM信息
     */
    private Map<String, Object> getJvmInfo() {
        Map<String, Object> jvmInfo = new HashMap<>();
        
        // 内存信息
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long heapMemoryUsed = memoryMXBean.getHeapMemoryUsage().getUsed() / 1024 / 1024;
        long heapMemoryMax = memoryMXBean.getHeapMemoryUsage().getMax() / 1024 / 1024;
        long nonHeapMemoryUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed() / 1024 / 1024;
        
        Map<String, Object> memory = new HashMap<>();
        memory.put("heapUsed", heapMemoryUsed + "MB");
        memory.put("heapMax", heapMemoryMax + "MB");
        memory.put("heapUsage", String.format("%.2f%%", (double) heapMemoryUsed / heapMemoryMax * 100));
        memory.put("nonHeapUsed", nonHeapMemoryUsed + "MB");
        jvmInfo.put("memory", memory);
        
        // 线程信息
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threads = new HashMap<>();
        threads.put("count", threadMXBean.getThreadCount());
        threads.put("peak", threadMXBean.getPeakThreadCount());
        threads.put("daemon", threadMXBean.getDaemonThreadCount());
        jvmInfo.put("threads", threads);
        
        return jvmInfo;
    }

    /**
     * 获取系统信息
     */
    private Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        
        systemInfo.put("processors", runtime.availableProcessors());
        systemInfo.put("totalMemory", totalMemory + "MB");
        systemInfo.put("freeMemory", freeMemory + "MB");
        systemInfo.put("maxMemory", maxMemory + "MB");
        systemInfo.put("usedMemory", (totalMemory - freeMemory) + "MB");
        
        return systemInfo;
    }

    /**
     * 检查数据库连接
     */
    private Map<String, Object> checkDatabase() {
        Map<String, Object> dbInfo = new HashMap<>();
        
        if (dataSource == null) {
            dbInfo.put("status", "UNKNOWN");
            dbInfo.put("message", "DataSource not configured");
            return dbInfo;
        }
        
        try (Connection connection = dataSource.getConnection()) {
            long startTime = System.currentTimeMillis();
            boolean isValid = connection.isValid(3);
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (isValid) {
                dbInfo.put("status", "UP");
                dbInfo.put("database", connection.getMetaData().getDatabaseProductName());
                dbInfo.put("version", connection.getMetaData().getDatabaseProductVersion());
                dbInfo.put("responseTime", responseTime + "ms");
            } else {
                dbInfo.put("status", "DOWN");
                dbInfo.put("message", "Database connection is not valid");
            }
        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage());
            dbInfo.put("status", "DOWN");
            dbInfo.put("error", e.getMessage());
        }
        
        return dbInfo;
    }

    /**
     * 检查Redis连接
     */
    private Map<String, Object> checkRedis() {
        Map<String, Object> redisInfo = new HashMap<>();
        
        if (redisTemplate == null) {
            redisInfo.put("status", "UNKNOWN");
            redisInfo.put("message", "RedisTemplate not configured");
            return redisInfo;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            String pingResult = redisTemplate.execute(connection -> {
                return connection.ping();
            });
            long responseTime = System.currentTimeMillis() - startTime;
            
            if ("PONG".equals(pingResult)) {
                redisInfo.put("status", "UP");
                redisInfo.put("responseTime", responseTime + "ms");
                
                // 获取Redis信息
                try {
                    Long dbSize = redisTemplate.execute(connection -> {
                        return connection.dbSize();
                    });
                    redisInfo.put("keys", dbSize);
                } catch (Exception e) {
                    log.warn("Failed to get Redis dbSize: {}", e.getMessage());
                }
            } else {
                redisInfo.put("status", "DOWN");
                redisInfo.put("message", "Redis ping failed");
            }
        } catch (Exception e) {
            log.error("Redis health check failed: {}", e.getMessage());
            redisInfo.put("status", "DOWN");
            redisInfo.put("error", e.getMessage());
        }
        
        return redisInfo;
    }

    /**
     * 检查消息队列
     */
    private Map<String, Object> checkMessageQueue() {
        Map<String, Object> mqInfo = new HashMap<>();
        mqInfo.put("pipeline", mqPipeline);
        
        switch (mqPipeline) {
            case "kafka":
                return checkKafka();
            case "rabbitMq":
                return checkRabbitMQ();
            case "redis":
                // Redis作为MQ时，使用Redis健康检查
                Map<String, Object> redisAsMq = checkRedis();
                redisAsMq.put("pipeline", "redis");
                return redisAsMq;
            case "eventBus":
            case "springEventBus":
                mqInfo.put("status", "UP");
                mqInfo.put("message", "In-memory event bus, always available");
                return mqInfo;
            default:
                mqInfo.put("status", "UNKNOWN");
                mqInfo.put("message", "Unknown MQ pipeline: " + mqPipeline);
                return mqInfo;
        }
    }

    /**
     * 检查Kafka连接
     */
    private Map<String, Object> checkKafka() {
        Map<String, Object> kafkaInfo = new HashMap<>();
        kafkaInfo.put("pipeline", "kafka");
        
        if (kafkaAdmin == null) {
            kafkaInfo.put("status", "UNKNOWN");
            kafkaInfo.put("message", "Kafka not configured");
            return kafkaInfo;
        }
        
        AdminClient adminClient = null;
        try {
            long startTime = System.currentTimeMillis();
            adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            
            // 获取集群ID，验证连接
            String clusterId = clusterResult.clusterId().get(5, TimeUnit.SECONDS);
            int nodeCount = clusterResult.nodes().get(5, TimeUnit.SECONDS).size();
            long responseTime = System.currentTimeMillis() - startTime;
            
            kafkaInfo.put("status", "UP");
            kafkaInfo.put("clusterId", clusterId);
            kafkaInfo.put("nodes", nodeCount);
            kafkaInfo.put("responseTime", responseTime + "ms");
        } catch (Exception e) {
            log.error("Kafka health check failed: {}", e.getMessage());
            kafkaInfo.put("status", "DOWN");
            kafkaInfo.put("error", e.getMessage());
        } finally {
            if (adminClient != null) {
                try {
                    adminClient.close();
                } catch (Exception e) {
                    log.warn("Failed to close Kafka admin client: {}", e.getMessage());
                }
            }
        }
        
        return kafkaInfo;
    }

    /**
     * 检查RabbitMQ连接
     */
    private Map<String, Object> checkRabbitMQ() {
        Map<String, Object> rabbitInfo = new HashMap<>();
        rabbitInfo.put("pipeline", "rabbitMq");
        
        if (rabbitTemplate == null) {
            rabbitInfo.put("status", "UNKNOWN");
            rabbitInfo.put("message", "RabbitMQ not configured");
            return rabbitInfo;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 通过获取连接工厂来验证连接
            org.springframework.amqp.rabbit.connection.Connection connection = 
                rabbitTemplate.getConnectionFactory().createConnection();
            
            if (connection.isOpen()) {
                long responseTime = System.currentTimeMillis() - startTime;
                rabbitInfo.put("status", "UP");
                rabbitInfo.put("responseTime", responseTime + "ms");
                
                // 获取连接信息
                try {
                    String host = connection.getDelegate().getAddress().getHostAddress();
                    int port = connection.getDelegate().getPort();
                    rabbitInfo.put("host", host);
                    rabbitInfo.put("port", port);
                } catch (Exception e) {
                    log.debug("Failed to get RabbitMQ connection details: {}", e.getMessage());
                }
                
                connection.close();
            } else {
                rabbitInfo.put("status", "DOWN");
                rabbitInfo.put("message", "RabbitMQ connection is not open");
            }
        } catch (Exception e) {
            log.error("RabbitMQ health check failed: {}", e.getMessage());
            rabbitInfo.put("status", "DOWN");
            rabbitInfo.put("error", e.getMessage());
        }
        
        return rabbitInfo;
    }

    /**
     * 就绪检查 - 用于K8s readiness probe
     *
     * @return 就绪状态
     */
    @GetMapping("/ready")
    @ApiOperation("/就绪检查")
    public Map<String, Object> readiness() {
        Map<String, Object> readyInfo = new HashMap<>();
        boolean isReady = true;
        
        // 检查关键组件
        Map<String, Object> dbStatus = checkDatabase();
        Map<String, Object> redisStatus = checkRedis();
        Map<String, Object> mqStatus = checkMessageQueue();
        
        if ("DOWN".equals(dbStatus.get("status"))) {
            isReady = false;
        }
        if ("DOWN".equals(redisStatus.get("status"))) {
            isReady = false;
        }
        if ("DOWN".equals(mqStatus.get("status"))) {
            isReady = false;
        }
        
        readyInfo.put("status", isReady ? "READY" : "NOT_READY");
        readyInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        readyInfo.put("database", dbStatus.get("status"));
        readyInfo.put("redis", redisStatus.get("status"));
        readyInfo.put("messageQueue", mqStatus.get("status"));
        readyInfo.put("mqPipeline", mqPipeline);
        
        return readyInfo;
    }

    /**
     * 存活检查 - 用于K8s liveness probe
     *
     * @return 存活状态
     */
    @GetMapping("/alive")
    @ApiOperation("/存活检查")
    public Map<String, String> liveness() {
        Map<String, String> aliveInfo = new HashMap<>();
        aliveInfo.put("status", "ALIVE");
        aliveInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return aliveInfo;
    }
}
