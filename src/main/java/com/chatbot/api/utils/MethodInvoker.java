package com.chatbot.api.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.chatbot.api.dto.MethodDetails;
import com.chatbot.api.dto.MethodResult;

@Service
public class MethodInvoker {
	
	private final WorkflowFunctionRegistry registry;
	private final BeanResolver beanResolver;
    private final MethodArgumentResolver argumentResolver;

	public MethodInvoker(WorkflowFunctionRegistry registry, BeanResolver beanResolver, MethodArgumentResolver argumentResolver) {
	    this.registry = registry;
	    this.beanResolver = beanResolver;
        this.argumentResolver = argumentResolver;
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

        Object[] args = argumentResolver.resolve(methodDetails, inputs);
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
