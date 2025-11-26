package com.java3y.austin.web.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Arrays;
import java.util.Locale;

/**
 * 国际化配置类
 * 配置语言解析器和消息资源管理器
 *
 * @author austin
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * 配置LocaleResolver - 从HTTP请求头Accept-Language提取语言标识
     * 支持中文(zh_CN)和英文(en_US)，默认为中文
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        // 设置默认语言为中文
        localeResolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        // 设置支持的语言列表
        localeResolver.setSupportedLocales(Arrays.asList(
                Locale.SIMPLIFIED_CHINESE,
                Locale.US
        ));
        return localeResolver;
    }

    /**
     * 配置MessageSource - 加载和管理多语言资源文件
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 设置资源文件基础名
        messageSource.setBasename("i18n/messages");
        // 设置资源文件编码为UTF-8
        messageSource.setDefaultEncoding("UTF-8");
        // 设置缓存时长为3600秒
        messageSource.setCacheSeconds(3600);
        // 设置使用消息代码作为默认消息（避免显示消息键）
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    /**
     * 配置LocaleChangeInterceptor - 支持通过请求参数切换语言
     * 可选功能：支持通过?lang=en参数切换语言
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        // 设置请求参数名为lang
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * 添加拦截器到注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
