package com.chatbot.api.utils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RuntimeTypeConverter {
    
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
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private Object convert(Object input, Class<?> targetType) {
    	if (input == null) return getDefaultValue(targetType);
        
        // If already correct type, return as-is
        if (targetType.isInstance(input.getClass())) {
            return input;
        }
        try {
            
            // Handle Map to Object conversion for complex types
        	if (input instanceof Map<?, ?> &&
        		    !targetType.isPrimitive() &&
        		    !ClassUtils.isPrimitiveOrWrapper(targetType) &&
        		    targetType != String.class)  {
                return convertMapToObject((Map<String, Object>) input, targetType);
            }
            
            if (targetType.isEnum()) {
                return Enum.valueOf((Class<Enum>) targetType, input.toString());
            }
            
        	// Use Spring's ConversionService for conversion
            if (conversionService.canConvert(input.getClass(), targetType)) {
                return conversionService.convert(input, targetType);
            }
            
            throw new IllegalArgumentException(
            	    "Cannot convert " + input.getClass().getName() +
            	    " to " + targetType.getName()
            	);
                        
        } catch (Exception e) {
            throw new RuntimeException("Type conversion failed for value: " + input + " to type: " + targetType, e);
        }
    }
    
    private Object convertMapToObject(Map<String, Object> map, Class<?> targetType) {
        try {
            return objectMapper.convertValue(map, targetType);
        } catch (Exception e) {
            throw new RuntimeException("Cannot convert map to " + targetType.getSimpleName(), e);
        }
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