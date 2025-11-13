package com.java3y.austin.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * OpenAPI 3.0 配置
 *
 * @author 3y
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI austinOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Austin消息推送平台API")
                        .description("Austin是一个消息推送平台，支持多种消息渠道的统一发送")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("3y")
                                .url("https://github.com/ZhongFuCheng3y/austin"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("本地环境"),
                        new Server().url("http://106.75.176.183:3000").description("演示环境")
                ));
    }
}
