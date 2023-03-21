package com.example.reggie.config;

import com.example.reggie.common.JacksonObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/static/backend/");
    }

    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 1.创建http消息json转换器
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 2.设置自定义json映射器
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        // 3.将自定义消息转换器放置到converters链的第一个，会被优先使用
        converters.add(0, messageConverter);
    }
}
