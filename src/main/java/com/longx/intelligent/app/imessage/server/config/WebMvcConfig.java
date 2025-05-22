package com.longx.intelligent.app.imessage.server.config;

import com.longx.intelligent.app.imessage.server.handler.WebHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by LONG on 2024/3/31 at 9:47 PM.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private WebHandlerInterceptor webHandlerInterceptor;

    @Bean
    public HttpMessageConverter<?> responseBodyConverter(){
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(responseBodyConverter());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webHandlerInterceptor);
    }

}
