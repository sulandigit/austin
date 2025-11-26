package com.java3y.austin.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * springdoc配置类 (替代swagger)
 *
 * @author 3y
 */
@Configuration
public class SwaggerConfiguration {
    /**
     * 对C端用户的接口文档
     * <p>
     * 地址：http://localhost:8080/swagger-ui/index.html
     *
     * @return
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("austin平台")
                        .description("消息推送接口接口文档")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("3y")
                                .url("http://gitee.com/zhongfucheng/austin")
                                .email("403686131@qq.com")));
    }

}