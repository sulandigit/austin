package com.java3y.austin.web.config;

import io.swagger.annotations.ApiModel;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;


/**
 * swagger配置类
 *
 * @author 3y
 */
@Component
@EnableOpenApi
@ApiModel
public class SwaggerConfiguration {
    /**
     * 对C端用户的接口文档（历史版本，包含无版本前缀的路径）
     * <p>
     * 地址：http://localhost:8080/swagger-ui/index.html
     *
     * @return
     */
    @Bean
    public Docket webApiDoc() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("用户端接口文档（历史版本）")
                .pathMapping("/")
                //定义是否开启Swagger，false是关闭，可以通过变量去控制，线上关闭
                .enable(true)
                //配置文档的元信息
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.java3y.austin.web.controller"))
                //匹配无版本前缀的路径（历史兼容）
                .paths(PathSelectors.regex("^(?!/api/).*$"))
                .build();
    }

    /**
     * API v1 版本接口文档
     * <p>
     * 地址：http://localhost:8080/swagger-ui/index.html
     *
     * @return
     */
    @Bean
    public Docket apiV1Doc() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("用户端接口文档-v1")
                .pathMapping("/")
                .enable(true)
                .apiInfo(apiInfoV1())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.java3y.austin.web.controller"))
                //匹配 /api/v1/ 开头的路径
                .paths(PathSelectors.ant("/api/v1/**"))
                .build();
    }

    /**
     * API v2 版本接口文档
     * <p>
     * 地址：http://localhost:8080/swagger-ui/index.html
     *
     * @return
     */
    @Bean
    public Docket apiV2Doc() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("用户端接口文档-v2")
                .pathMapping("/")
                .enable(true)
                .apiInfo(apiInfoV2())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.java3y.austin.web.controller"))
                //匹配 /api/v2/ 开头的路径
                .paths(PathSelectors.ant("/api/v2/**"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("austin平台")
                .description("消息推送接口接口文档（历史版本，建议迁移到 v2）")
                .contact(new Contact("3y", "http://gitee.com/zhongfucheng/austin", "403686131@qq.com"))
                .version("v1.0")
                .build();
    }

    private ApiInfo apiInfoV1() {
        return new ApiInfoBuilder()
                .title("austin平台 API v1")
                .description("消息推送接口 v1 版本（稳定版本，建议迁移到 v2）")
                .contact(new Contact("3y", "http://gitee.com/zhongfucheng/austin", "403686131@qq.com"))
                .version("v1")
                .build();
    }

    private ApiInfo apiInfoV2() {
        return new ApiInfoBuilder()
                .title("austin平台 API v2")
                .description("消息推送接口 v2 版本（推荐使用）")
                .contact(new Contact("3y", "http://gitee.com/zhongfucheng/austin", "403686131@qq.com"))
                .version("v2")
                .build();
    }

}