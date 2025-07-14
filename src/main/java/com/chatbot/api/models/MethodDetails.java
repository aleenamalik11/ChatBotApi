package com.chatbot.api.models;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodDetails {
	private Method method;
    private Class<?> declaringClass;
    private Parameter[] parameters;
    private Class<?>[] parameterTypes;
    private String methodSignature;
    
    public MethodDetails(Method method, Class<?> declaringClass) {
        this.method = method;
        this.declaringClass = declaringClass;
        this.parameters = method.getParameters();
        this.parameterTypes = method.getParameterTypes();
        this.methodSignature = buildMethodSignature(method);
    }
    
    private String buildMethodSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getName()).append("(");
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(types[i].getSimpleName());
        }
        sb.append(")");
        return sb.toString();
    }
}