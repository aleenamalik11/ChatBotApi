package com.chatbot.api.helperservices;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.chatbot.api.dto.MethodDetails;
import com.chatbot.api.dto.MethodResult;
import com.chatbot.api.utils.RuntimeTypeConverter;

@Service
public class MethodInvoker {
	
	private final RuntimeTypeConverter converter;
	private final WorkflowFunctionRegistry registry;
	private final BeanResolver beanResolver;

	public MethodInvoker(RuntimeTypeConverter converter, WorkflowFunctionRegistry registry, BeanResolver beanResolver) {
	    this.converter = converter;
	    this.registry = registry;
	    this.beanResolver = beanResolver;
	}

    
    public MethodResult invoke(Map<String, Object> inputs, String command) {
        try {
        	MethodDetails method = registry.get(command);
            
            if (method == null) {
                return failure(command, "method not found");
            }
                       
            return invokeMethod(method, inputs);
            
        } catch (Exception ex) {
            return failure(command, rootCauseMessage(ex));
        }
    }

    public MethodResult invoke(Map<String, Object> inputs, String command, String packageName) {
        return invoke(inputs, command);
    }
        
    private MethodResult invokeMethod(MethodDetails methodDetails, Map<String, Object> inputs) throws Exception {
    	Object instance = beanResolver.getBean(methodDetails.getBeanName());

        Object[] args = prepareMethodArguments(methodDetails, inputs);
        Object output;
        try {
            output = methodDetails.getMethod().invoke(instance, args);
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof Exception exception) {
                throw exception;
            }
            throw ex;
        }
        
        System.out.println("Successfully invoked " + methodDetails.getMethod().getName());
        return new MethodResult(output, "success");
    }
    
    
    
    private Object[] prepareMethodArguments(MethodDetails methodDetails, Map<String, Object> inputs) {
        Parameter[] parameters = methodDetails.getMethod().getParameters();
        Object[] args = new Object[parameters.length];
        
        try {
            
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                Class<?> paramType = param.getType();
                WorkflowParam annotation = param.getAnnotation(WorkflowParam.class);

                if (annotation == null) {
                    throw new IllegalStateException(
                        "Parameter '" + param.getName() + "' must have @WorkflowParam."
                    );
                }

                if (inputs == null || !inputs.containsKey(annotation.name())) {
                    throw new IllegalArgumentException(
                        "Missing input '" + annotation.name() + "' for parameter '" + param.getName() + "'."
                    );
                }

                args[i] = converter.castToRuntimeType(inputs.get(annotation.name()), paramType);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to prepare method arguments", ex);
        }
        
        return args;
    }
    
    private MethodResult failure(String command, String rootCause) {
        return new MethodResult(null, "Command '" + command + "' failed: " + rootCause);
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() != null ? current.getMessage() : current.getClass().getSimpleName();
    }
}
