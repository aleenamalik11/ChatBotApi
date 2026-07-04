package com.chatbot.api.helperservices;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.chatbot.api.dto.MethodDetails;
import com.chatbot.api.dto.MethodResult;
import com.chatbot.api.utils.RuntimeTypeConverter;

@Service
public class MethodInvoker {
	
	private final ApplicationContext applicationContext;

	private final RuntimeTypeConverter converter;

	public MethodInvoker(ApplicationContext context,
	                     RuntimeTypeConverter converter) {
	    this.applicationContext = context;
	    this.converter = converter;
	}
    
    private final Map<String, MethodDetails> methodCache = new ConcurrentHashMap<>();
    
    public MethodResult invoke(Map<String, Object> inputs, String command) {
        try {
            MethodDetails method = getMethodsFromCache(command);
            
            if (method == null) {
                return new MethodResult(null, "Method not found for: " + command);
            }
                       
            try {
                MethodResult result = invokeMethod(method, inputs);
                if ("success".equals(result.getResult())) {
                    return result;
                }
            } catch (Exception e) {
                System.out.println("Error invoking method " + method.getMethod().getName() + ": " + e.getMessage());
                // Continue to try next method
            }
            
            // If we get here, all methods failed
            return new MethodResult(null, "failure");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return new MethodResult(null, "failure");
        }
    }
        
    private MethodResult invokeMethod(MethodDetails methodDetails, Map<String, Object> inputs) throws Exception {
        Object instance = methodDetails.getBean();

        Object[] args = prepareMethodArguments(methodDetails, inputs);
        Object output = methodDetails.getMethod().invoke(instance, args);
        
        System.out.println("Successfully invoked " + methodDetails.getMethod().getName());
        return new MethodResult(output, "success");
    }
    
    private MethodDetails getMethodsFromCache(String command) {
        
    	MethodDetails method = methodCache.get(command);

    	if (method != null)
    	    return method;

    	method = scanMethods(command);

    	if (method != null)
    	    methodCache.put(command, method);

    	return method;
    }
    
    private MethodDetails scanMethods(String command) {

        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);

            for (Method method : bean.getClass().getMethods()) {

                WorkflowFunction annotation =
                        method.getAnnotation(WorkflowFunction.class);

                if (annotation != null && annotation.name().equals(command)) {

                	MethodDetails methodDetails = new MethodDetails(method, bean);

                    return methodDetails;
                }
            }
        }

        return null;
    }
    
    private Object[] prepareMethodArguments(MethodDetails methodDetails, Map<String, Object> inputs) {
        Parameter[] parameters = methodDetails.getMethod().getParameters();
        Object[] args = new Object[parameters.length];
        
        try {
            
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                Class<?> paramType = param.getType();
                Object value = null;
                
                if (!isSimple(paramType)) {
                    value = converter.castToRuntimeType(inputs, paramType);
                } else {
                	WorkflowParam annotation = param.getAnnotation(WorkflowParam.class);

                    if (annotation == null) {
                        throw new IllegalStateException(
                            "Simple parameter '" + param.getName() +
                            "' must have @WorkflowParam."
                        );
                    }
                    
                    value = converter.castToRuntimeType(
                            inputs.get(annotation.name()),
                            paramType
                        );
                    
                }
                args[i] = value;
            }
        } catch (Exception ex) {
            System.err.println("Error preparing method arguments: " + ex.getMessage());
            throw new RuntimeException("Failed to prepare method arguments", ex);
        }
        
        return args;
    }
    
    private boolean isSimple(Class<?> type) {
        return type.isPrimitive() || 
               type.equals(String.class) || 
               Number.class.isAssignableFrom(type) ||
               type.equals(Boolean.class) ||
               type.equals(Character.class) ||
               type.equals(Enum.class);
    }
}