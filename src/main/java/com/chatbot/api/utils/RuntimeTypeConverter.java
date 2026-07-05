package com.chatbot.api.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JavaType;
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
    
    public Object castToRuntimeType(Object input, Class<?> type) {
        return convert(input, type);
    }

    public Object castToRuntimeType(Object input, Type type) {
        return convert(input, type);
    }
    
    public Class<?> getRuntimeType(Object value) {
        return value != null ? value.getClass() : null;
    }
    
	private Object convert(Object input, Type targetType) {
        return convert(input, objectMapper.getTypeFactory().constructType(targetType));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private Object convert(Object input, JavaType targetType) {
        Class<?> rawType = targetType.getRawClass();

    	if (input == null) {
            return getDefaultValue(rawType);
        }

        if (rawType.isInstance(input) && !targetType.isContainerType() && !rawType.isArray()) {
            return input;
        }

        try {
            if (rawType.isEnum()) {
                return convertEnum(input, (Class<? extends Enum>) rawType);
            }

            if (rawType.isArray()) {
                return convertArray(input, targetType);
            }

            if (Collection.class.isAssignableFrom(rawType)) {
                return convertCollection(input, targetType);
            }

            if (input instanceof Map<?, ?> || Map.class.isAssignableFrom(rawType)) {
                return objectMapper.convertValue(input, targetType);
            }

            if (conversionService.canConvert(input.getClass(), rawType)) {
                return conversionService.convert(input, rawType);
            }

            return objectMapper.convertValue(input, targetType);
                        
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Cannot convert value '" + input + "' (" + input.getClass().getName() +
                ") to " + targetType.toCanonical() + ": " + rootCauseMessage(e),
                e
            );
        }
    }

    private Object convertArray(Object input, JavaType targetType) {
        JavaType componentType = targetType.getContentType();
        Class<?> componentClass = targetType.getRawClass().getComponentType();
        Collection<?> values = toCollection(input, targetType);
        Object array = Array.newInstance(componentClass, values.size());

        int index = 0;
        for (Object value : values) {
            Array.set(array, index, convert(value, componentType));
            index++;
        }

        return array;
    }

    private Collection<?> convertCollection(Object input, JavaType targetType) {
        JavaType elementType = targetType.getContentType();
        Collection<?> values = toCollection(input, targetType);
        Collection<Object> converted = newCollection(targetType.getRawClass());

        for (Object value : values) {
            converted.add(convert(value, elementType));
        }

        return converted;
    }

    private Collection<?> toCollection(Object input, JavaType targetType) {
        if (input instanceof Collection<?> collection) {
            return collection;
        }

        if (input.getClass().isArray()) {
            int length = Array.getLength(input);
            Collection<Object> values = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                values.add(Array.get(input, i));
            }
            return values;
        }

        throw new IllegalArgumentException(
            "Expected collection or array input for " + targetType.toCanonical() +
            " but received " + input.getClass().getName()
        );
    }

    private Collection<Object> newCollection(Class<?> rawType) {
        if (Set.class.isAssignableFrom(rawType)) {
            return new LinkedHashSet<>();
        }

        return new ArrayList<>();
    }

    @SuppressWarnings("rawtypes")
    private Enum<?> convertEnum(Object input, Class<? extends Enum> enumType) {
        String rawValue = input.toString();
        String normalizedValue = normalizeEnumValue(rawValue);

        for (Enum<?> enumConstant : enumType.getEnumConstants()) {
            if (enumConstant.name().equalsIgnoreCase(rawValue) ||
                normalizeEnumValue(enumConstant.name()).equals(normalizedValue) ||
                enumConstant.toString().equalsIgnoreCase(rawValue)) {
                return enumConstant;
            }
        }

        throw new IllegalArgumentException(
            "Invalid enum value '" + rawValue + "' for " + enumType.getSimpleName() +
            ". Expected one of: " + String.join(", ", enumNames(enumType))
        );
    }

    private String normalizeEnumValue(String value) {
        return value.trim().replace('-', '_').replace(' ', '_').toUpperCase();
    }

    @SuppressWarnings("rawtypes")
    private String[] enumNames(Class<? extends Enum> enumType) {
        Enum[] constants = enumType.getEnumConstants();
        String[] names = new String[constants.length];
        for (int i = 0; i < constants.length; i++) {
            names[i] = constants[i].name();
        }
        return names;
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

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() != null ? current.getMessage() : current.getClass().getSimpleName();
    }
}
