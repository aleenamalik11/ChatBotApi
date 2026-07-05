package com.chatbot.api.dto;

import java.lang.reflect.Method;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodDetails {
	 private Method method;
	 private String beanName;
     private ParameterMetadata[] parameters;
    
    public MethodDetails(Method method, String beanName, ParameterMetadata[] parameters) {
        this.method = method;
        this.beanName = beanName;
        this.parameters = parameters;
    }
    
}
