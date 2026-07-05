package com.chatbot.api.utils;

public interface BeanResolver {

    String[] getBeanNames();

    Class<?> getBeanType(String beanName);

    Object getBean(String beanName);
}
