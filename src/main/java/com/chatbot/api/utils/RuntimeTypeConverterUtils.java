package com.chatbot.api.utils;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

public class RuntimeTypeConverterUtils {
    
    public static Object castToRuntimeType(Object input, String type) throws ClassNotFoundException {
        ConversionService conversionService = new DefaultConversionService();
        return conversionService.convert(input, Class.forName(type));
    }
    
    public static Class<?> getRuntimeType(Object value) {
        return value != null ? value.getClass() : null;
    }
}
