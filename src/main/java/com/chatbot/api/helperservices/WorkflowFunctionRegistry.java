package com.chatbot.api.helperservices;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.chatbot.api.dto.MethodDetails;

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
                validateWorkflowFunction(command, method, beanName);

                if (methodCache.containsKey(command)) {
                    throw new IllegalStateException(
                        "Duplicate workflow command '" + command + "' found on " +
                        describe(method, beanName) + " and " +
                        describe(methodCache.get(command).getMethod(), methodCache.get(command).getBeanName()));
                }

                methodCache.put(
                    command,
                    new MethodDetails(method, beanName)
                );
            }
        }
    }

    private void validateWorkflowFunction(String command, Method method, String beanName) {
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

        Set<String> parameterNames = new HashSet<>();
        for (Parameter parameter : method.getParameters()) {
            WorkflowParam workflowParam = parameter.getAnnotation(WorkflowParam.class);
            if (workflowParam == null || workflowParam.name().trim().isBlank()) {
                throw new IllegalStateException(
                    "Missing @WorkflowParam for command '" + command + "' parameter '" +
                    parameter.getName() + "' on " + describe(method, beanName));
            }

            String parameterName = workflowParam.name().trim();
            if (!parameterNames.add(parameterName)) {
                throw new IllegalStateException(
                    "Duplicate workflow parameter name '" + parameterName +
                    "' for command '" + command + "' on " + describe(method, beanName));
            }

            if (!isSupportedParameterType(parameter.getType())) {
                throw new IllegalStateException(
                    "Unsupported workflow parameter type '" + parameter.getType().getName() +
                    "' for parameter '" + parameterName + "' on command '" + command + "'.");
            }
        }
    }

    private boolean isSupportedParameterType(Class<?> type) {
        if (type.isPrimitive() ||
            type.equals(String.class) ||
            Number.class.isAssignableFrom(type) ||
            type.equals(Boolean.class) ||
            type.equals(Character.class) ||
            type.isEnum()) {
            return true;
        }

        return !type.isArray() &&
               !type.equals(Object.class) &&
               !type.isInterface() &&
               !Modifier.isAbstract(type.getModifiers()) &&
               !Collection.class.isAssignableFrom(type) &&
               !Map.class.isAssignableFrom(type);
    }

    private String describe(Method method, String beanName) {
        return beanName + "#" + method.getName();
    }
   
}
