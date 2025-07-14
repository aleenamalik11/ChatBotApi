package com.chatbot.api.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.chatbot.api.helperservices.MethodInvoker;

@Component
public class SpringBeanProvider implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
    
    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }
    
    public static RuntimeTypeConverter getConverterUtils() {
        return applicationContext.getBean(RuntimeTypeConverter.class);
    }
    
    public static MethodInvoker getMethodInvoker() {
        return applicationContext.getBean(MethodInvoker.class);
    }
}
