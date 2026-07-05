package com.chatbot.api.utils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.chatbot.api.annotations.WorkflowFunction;
import com.chatbot.api.annotations.WorkflowParam;
import com.chatbot.api.dto.MethodDetails;
import com.chatbot.api.dto.ParameterMetadata;

import jakarta.annotation.PostConstruct;

@Service
public class WorkflowFunctionRegistry {

    private final BeanResolver beanResolver;

    private final Map<String, MethodDetails> methodCache = new ConcurrentHashMap<>();

    public WorkflowFunctionRegistry(BeanResolver beanResolver) {
        this.beanResolver = beanResolver;
    }

    @PostConstruct
    public void initialize() {
    	registerWorkflowFunctions();
    }

    public MethodDetails get(String command) {
        return methodCache.get(command);
    }
    
    private void registerWorkflowFunctions() {
        methodCache.clear();
    	String[] beanNames = beanResolver.getBeanNames();

        for (String beanName : beanNames) {

            Class<?> beanType = beanResolver.getBeanType(beanName);
            if (beanType == null) {
                continue;
            }

            for (Method method : beanType.getMethods()) {

                WorkflowFunction wf = method.getAnnotation(WorkflowFunction.class);

                if (wf == null)
                    continue;

                String command = wf.name().trim();
                ParameterMetadata[] parameters = validateWorkflowFunction(command, method, beanName);

                if (methodCache.containsKey(command)) {
                    throw new IllegalStateException(
                        "Duplicate workflow command '" + command + "' found on " +
                        describe(method, beanName) + " and " +
                        describe(methodCache.get(command).getMethod(), methodCache.get(command).getBeanName()));
                }

                methodCache.put(
                    command,
                    new MethodDetails(method, beanName, parameters)
                );
            }
        }
    }

    private ParameterMetadata[] validateWorkflowFunction(String command, Method method, String beanName) {
        if (command.isBlank()) {
            throw new IllegalStateException(
                "Workflow command name cannot be blank on " + describe(method, beanName));
        }

        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers)) {
            throw new IllegalStateException(
                "Invalid workflow method signature for command '" + command + "' on " +
                describe(method, beanName) + ": method must be public, concrete, and non-static.");
        }

        Parameter[] methodParameters = method.getParameters();
        ParameterMetadata[] parameterMetadata = new ParameterMetadata[methodParameters.length];
        Set<String> parameterNames = new HashSet<>();
        for (int i = 0; i < methodParameters.length; i++) {
            Parameter parameter = methodParameters[i];
            WorkflowParam workflowParam = parameter.getAnnotation(WorkflowParam.class);
            if (workflowParam == null || workflowParam.name().trim().isBlank()) {
                throw new IllegalStateException(
                    "Missing @WorkflowParam for command '" + command + "' parameter '" +
                    parameter.getName() + "' on " + describe(method, beanName));
            }

            String parameterName = workflowParam.name().trim();

            Type genericType = parameter.getParameterizedType();
            if (!isSupportedParameterType(genericType)) {
                throw new IllegalStateException(
                    "Unsupported workflow parameter type '" + genericType.getTypeName() +
                    "' for parameter '" + parameterName + "' on command '" + command + "'.");
            }

            parameterMetadata[i] = new ParameterMetadata(
                parameterName,
                parameter.getName(),
                parameter.getType(),
                genericType
            );
        }

        return parameterMetadata;
    }

    private boolean isSupportedParameterType(Type type) {
        if (type instanceof Class<?> clazz) {
            return isSupportedClass(clazz);
        }

        if (type instanceof GenericArrayType genericArrayType) {
            return isSupportedParameterType(genericArrayType.getGenericComponentType());
        }

        if (type instanceof ParameterizedType parameterizedType) {
            Class<?> rawType = rawClass(parameterizedType);
            if (rawType == null) {
                return false;
            }

            if (Collection.class.isAssignableFrom(rawType)) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                return typeArguments.length == 1 && isSupportedParameterType(typeArguments[0]);
            }

            if (Map.class.isAssignableFrom(rawType)) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                return typeArguments.length == 2 &&
                       typeArguments[0].equals(String.class) &&
                       isSupportedParameterType(typeArguments[1]);
            }

            return isSupportedClass(rawType);
        }

        return false;
    }

    private boolean isSupportedClass(Class<?> type) {
        if (type.isArray()) {
            return isSupportedParameterType(type.getComponentType());
        }

        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            return false;
        }

        if (type.isPrimitive() ||
            type.equals(String.class) ||
            Number.class.isAssignableFrom(type) ||
            type.equals(Boolean.class) ||
            type.equals(Character.class) ||
            type.isEnum()) {
            return true;
        }

        return !type.equals(Object.class) &&
               !type.isInterface() &&
               !Modifier.isAbstract(type.getModifiers());
    }

    private Class<?> rawClass(ParameterizedType type) {
        Type rawType = type.getRawType();
        if (rawType instanceof Class<?> rawClass) {
            return rawClass;
        }

        return null;
    }

    private String describe(Method method, String beanName) {
        return beanName + "#" + method.getName();
    }
   
}
