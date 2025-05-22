package com.longx.intelligent.app.imessage.server.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonUtil {
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 如果json中有新增的字段并且是实体类类中不存在的，不报错
        MAPPER.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        // 如果存在未知属性，则忽略不报错
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许key没有双引号
        MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 允许key有单引号
        MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 允许整数以0开头
        MAPPER.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
        // 允许字符串中存在回车换行控制符
        MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    public static <T> String toJson(T obj) {
        return toJson(obj, false);
    }

    public static <T> String toFormattedJson(T obj){
        return toJson(obj, true);
    }

    private static <T> String toJson(T obj, boolean format) {
        try {
            return format ? MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj) : MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new JsonException("转换对象到 Json 出错", e);
        }
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new JsonException("转换 Json 到对象出错", e);
        }
    }

    public static <T> List<T> toObjectList(String json, Class<T> clazz) {
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, clazz);
        try {
            return MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            throw new JsonException("转换 Json 到对象列表出错", e);
        }
    }

    public static <T> T convertValue(Object object, Class<T> clazz){
        return MAPPER.convertValue(object, clazz);
    }

    public static <T> T convertValue(Object object, TypeReference<T> toValueTypeRefz){
        return MAPPER.convertValue(object, toValueTypeRefz);
    }

    public static class JsonException extends RuntimeException {
        public JsonException() {
            super();
        }

        public JsonException(String message) {
            super(message);
        }

        public JsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static void writeObjectToJsonFile(Object object, File file) {
        try {
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            MAPPER.writeValue(file, object);
        } catch (IOException e) {
            throw new JsonException("写入对象到 Json 文件出错", e);
        }
    }

    public static <T> T loadObjectFromJsonFile(File file, Class<T> clazz) {
        try {
            return MAPPER.readValue(file, clazz);
        } catch (IOException e) {
            throw new JsonException("从 Json 文件读取对象出错", e);
        }
    }

    public static <T> T loadObjectFromJsonFile(File file, TypeReference<T> valueTypeRef) {
        try {
            return MAPPER.readValue(file, valueTypeRef);
        } catch (IOException e) {
            throw new JsonException("从 Json 文件读取对象出错", e);
        }
    }
}
