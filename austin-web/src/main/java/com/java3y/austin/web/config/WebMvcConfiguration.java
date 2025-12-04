package com.java3y.austin.web.config;

import com.java3y.austin.web.interceptor.SignatureInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册签名验证拦截器
 *
 * @author austin
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private SignatureInterceptor signatureInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册签名验证拦截器
        registry.addInterceptor(signatureInterceptor)
                // 拦截/send、/batchSend、/recall等API
                .addPathPatterns("/send", "/batchSend", "/recall")
                // 排除不需要签名验证的路径（如Swagger文档、健康检查等）
                .excludePathPatterns(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v2/api-docs",
                        "/v3/api-docs",
                        "/webjars/**",
                        "/actuator/**",
                        "/error"
                );
    }

}
