package com.chatbot.api.engine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.stereotype.Component;

import com.chatbot.api.models.Workflow;
import com.chatbot.api.services.SpringUtils;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Getter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("custom_logic")
@Component
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
                System.out.println("Spring found: " + clazz.getName());
                
                try {
                    Method method = clazz.getDeclaredMethod(function, Map.class);
                    
                    // Use static context instead of @Autowired
                    Object instance = SpringUtils.getBean(clazz);
                    method.invoke(instance, workflow.inputs);
                    
                    System.out.println("Successfully invoked " + function + " method");
                    break;
                } catch (NoSuchMethodException e) {
                    System.out.println("Method not found: " + function + " in " + clazz.getSimpleName());
                } catch (Exception e) {
                    System.out.println("Error invoking method: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return "success";
        } catch (Exception ex) {
            return "failure";
        }
    }
}