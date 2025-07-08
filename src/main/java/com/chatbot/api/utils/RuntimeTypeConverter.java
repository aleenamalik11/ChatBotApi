package com.chatbot.api.utils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RuntimeTypeConverter {
    
    // Remove static and fix injection
    @Autowired
    private ConversionService conversionService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Static methods that delegate to instance methods
    public Object castToRuntimeType(Object input, String type) throws ClassNotFoundException {
        return convert(input, Class.forName(type));
    }
    
    public Object castToRuntimeType(Object input, Class<?> type) throws ClassNotFoundException {
        return convert(input, type);
    }
    
    public Class<?> getRuntimeType(Object value) {
        return value != null ? value.getClass() : null;
    }
    
    @SuppressWarnings("unchecked")
	private Object convert(Object value, Class<?> targetType) {
        if (value == null) return getDefaultValue(targetType);
        
        // If already correct type, return as-is
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        
        try {
            // Use Spring's ConversionService for conversion
            if (conversionService.canConvert(value.getClass(), targetType)) {
                return conversionService.convert(value, targetType);
            }
            
            // Handle Map to Object conversion for complex types
            if (value instanceof Map && !targetType.isPrimitive() && !isWrapperType(targetType)) {
                return convertMapToObject((Map<String, Object>) value, targetType);
            }
            
            // Fallback to string conversion if ConversionService can't handle it
            if (conversionService.canConvert(String.class, targetType)) {
                return conversionService.convert(value.toString(), targetType);
            }
            
            throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to " + targetType);
            
        } catch (Exception e) {
            throw new RuntimeException("Type conversion failed for value: " + value + " to type: " + targetType, e);
        }
    }
    
    private Object convertMapToObject(Map<String, Object> map, Class<?> targetType) {
        try {
            return objectMapper.convertValue(map, targetType);
        } catch (Exception e) {
            throw new RuntimeException("Cannot convert map to " + targetType.getSimpleName(), e);
        }
    }
    
    private boolean isWrapperType(Class<?> type) {
        return type == Boolean.class || type == Integer.class || type == Character.class ||
               type == Byte.class || type == Short.class || type == Double.class ||
               type == Long.class || type == Float.class || type == String.class;
    }
    
    private Object getDefaultValue(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == double.class) return 0.0;
            if (type == float.class) return 0.0f;
            if (type == boolean.class) return false;
            if (type == char.class) return '\0';
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
        }
        return null;
    }
}