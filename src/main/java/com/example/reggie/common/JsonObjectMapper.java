package com.example.reggie.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonObjectMapper extends ObjectMapper {
    public JsonObjectMapper() {
        super();
//
//        // 1.新建自定义映射模块
//        SimpleModule simpleModule = new SimpleModule();
//        // 1.1 将Java Long整形映射为String
//        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
//        simpleModule.addSerializer()
//
//        this.registerModule();
    }
}
