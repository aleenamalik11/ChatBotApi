package com.chatbot.api.helperservices;

public interface BeanResolver {

    String[] getBeanNames();

    Class<?> getBeanType(String beanName);

    Object getBean(String beanName);
}
