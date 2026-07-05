package com.chatbot.api.dto;

import java.lang.reflect.Type;

import lombok.Getter;

@Getter
public class ParameterMetadata {

    private final String inputName;
    private final String methodParameterName;
    private final Class<?> parameterType;
    private final Type genericType;

    public ParameterMetadata(String inputName, String methodParameterName, Class<?> parameterType, Type genericType) {
        this.inputName = inputName;
        this.methodParameterName = methodParameterName;
        this.parameterType = parameterType;
        this.genericType = genericType;
    }
}
