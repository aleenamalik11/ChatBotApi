package com.chatbot.api.dto;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodDetails {
	 private Method method;
	 private String beanName;
    
    public MethodDetails(Method method, String beanName) {
        this.method = method;
        this.beanName = beanName;
    }
    
}