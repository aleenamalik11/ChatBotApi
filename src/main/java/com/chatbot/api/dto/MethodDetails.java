package com.chatbot.api.dto;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodDetails {
	 private Method method;
	 private Object bean;
    
    public MethodDetails(Method method, Object bean) {
        this.method = method;
        this.bean = bean;
    }
    
}