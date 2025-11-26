package com.java3y.austin.cron.xxl.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

/**
 * XxlJob配置类
 * <p>
 * 该配置类用于创建和配置XxlJob执行器
 * 只有当配置项 austin.xxl.job.enabled=true 时才会生效
 * </p>
 *
 * @author 3y
 */
@Slf4j
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "xxl.job")
@ConditionalOnProperty(name = "austin.xxl.job.enabled", havingValue = "true")
public class XxlJobConfig {

    /**
     * 调度中心地址列表,多个地址用逗号分隔
     */
    @NotBlank(message = "XxlJob调度中心地址不能为空")
    private String adminAddresses;

    /**
     * 访问令牌,用于调度中心与执行器之间的认证
     */
    private String accessToken;

    /**
     * 执行器配置
     */
    private Executor executor = new Executor();

    /**
     * 执行器配置项
     */
    @Data
    public static class Executor {
        /**
         * 执行器应用名称
         */
        @NotBlank(message = "执行器应用名称不能为空")
        private String appname;

        /**
         * 执行器注册地址,优先使用该配置作为注册地址
         * 为空时使用内嵌服务 "IP:PORT" 作为注册地址
         */
        private String address;

        /**
         * 执行器IP,为空时自动获取
         */
        private String ip;

        /**
         * 执行器端口号
         */
        @Positive(message = "执行器端口号必须大于0")
        private int port = 9999;

        /**
         * 执行器日志文件存储路径
         */
        @NotBlank(message = "执行器日志路径不能为空")
        private String logpath;

        /**
         * 执行器日志保留天数
         */
        @Positive(message = "日志保留天数必须大于0")
        private int logretentiondays = 30;
    }

    /**
     * 创建XxlJob执行器Bean
     *
     * @return XxlJobSpringExecutor实例
     */
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> 开始初始化XxlJob执行器 <<<<<<<<<<<<");
        log.info("XxlJob调度中心地址: {}", adminAddresses);
        log.info("XxlJob执行器应用名称: {}", executor.getAppname());
        log.info("XxlJob执行器端口: {}", executor.getPort());

        // 创建执行器
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(executor.getAppname());
        xxlJobSpringExecutor.setAddress(executor.getAddress());
        xxlJobSpringExecutor.setIp(executor.getIp());
        xxlJobSpringExecutor.setPort(executor.getPort());
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(executor.getLogpath());
        xxlJobSpringExecutor.setLogRetentionDays(executor.getLogretentiondays());

        log.info(">>>>>>>>>>> XxlJob执行器初始化完成 <<<<<<<<<<<<");
        return xxlJobSpringExecutor;
    }

}
