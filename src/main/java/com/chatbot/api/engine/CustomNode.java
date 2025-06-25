package com.chatbot.api.engine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.springframework.data.annotation.TypeAlias;

import com.chatbot.api.models.Workflow;
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
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CustomNode extends WorkflowNode
{

	private String function;
	private Map<String, Object> inputs;
	
	@Override
	public String performExecution(Workflow workflow) {
		
		// Scan package where your workflow classes are located
        Reflections reflections = new Reflections("com.example.workflow"); // your package here

        // Get all classes in the package
        Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);

        for (Class<?> clazz : allClasses) {
            if (clazz.getSimpleName().startsWith("workflow")) {
                System.out.println("Found: " + clazz.getSimpleName());

                Object instance = null;
				try {
					instance = clazz.getDeclaredConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

                try {
                    Method method = clazz.getMethod(function); // assumes no args
                    try {
						method.invoke(instance);
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                } catch (NoSuchMethodException e) {
                    System.out.println("Method not found: " + function + " in " + clazz.getSimpleName());
                }
            }
        }
    
		return "";
	}
}
