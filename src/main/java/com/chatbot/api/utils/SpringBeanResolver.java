package com.chatbot.api.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringBeanResolver implements BeanResolver {

    private final ApplicationContext applicationContext;

    public SpringBeanResolver(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public String[] getBeanNames() {
        return applicationContext.getBeanDefinitionNames();
    }

    @Override
    public Class<?> getBeanType(String beanName) {
        return applicationContext.getType(beanName);
    }

    @Override
    public Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }
}
