package com.chatbot.api.engine;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.data.annotation.TypeAlias;

import com.chatbot.api.models.Workflow;
import com.chatbot.api.utils.RuntimeTypeConverter;
import com.chatbot.api.utils.SpringBeanProvider;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("custom_logic")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CustomNode extends WorkflowNode {
    
    private String function;
    
    @Override
    public String performExecution(Workflow workflow) {
        try {
            ClassPathScanningCandidateComponentProvider scanner =
                    new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));
            Set<BeanDefinition> candidates = scanner.findCandidateComponents("com.chatbot.customservices");
            
            for (BeanDefinition bean : candidates) {
                Class<?> clazz = Class.forName(bean.getBeanClassName());
                
                // Find all methods with the target name
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getName().equals(function)) {
                        try {
                            Object instance = SpringBeanProvider.getBean(clazz);
                            Object[] args = prepareMethodArguments(method, workflow.inputs);
                            method.invoke(instance, args);
                            
                            System.out.println("Successfully invoked " + function);
                            return "success";
                        } catch (Exception e) {
                            System.out.println("Error invoking method: " + e.getMessage());
                            continue; // Try next method with same name
                        }
                    }
                }
            }
            
            return "Method not found";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "failure";
        }
    }
    
    private Object[] prepareMethodArguments(Method method, Map<String, Object> inputs) {
    	
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
    	try {
	        
	        for (int i = 0; i < parameters.length; i++) {
	            Parameter param = parameters[i];
	            String paramName = param.getName();
	            Class<?> paramType = param.getType();
	            
	            // Try to get value from inputs map
	            Object value = inputs.get(paramName);

	            RuntimeTypeConverter converterUtils = SpringBeanProvider.getConverterUtils();
	            // If value is null and type is a complex object, try converting the whole map
	            if (value == null && !isSimple(paramType)) {
	                value = converterUtils.castToRuntimeType(inputs, paramType);
	            } else {
	                value = converterUtils.castToRuntimeType(value, paramType);
	            }

	            args[i] = value;
	        }
    	}
    	catch(Exception ex) {
    		System.out.println("Error invoking method: " + ex.getMessage());
    	}
        
        return args;
    }
    
    private static boolean isSimple(Class<?> type) {
        return type.isPrimitive() || type.equals(String.class) || Number.class.isAssignableFrom(type);
    }
}