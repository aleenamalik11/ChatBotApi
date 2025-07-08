package com.chatbot.api.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;

import com.chatbot.api.models.User;

@Configuration
public class ConversionServiceConfig {
    
    @Bean
    @Primary
    public ConversionService conversionService() {
        DefaultConversionService service = new DefaultConversionService();
        
        // Add custom converters if needed
        service.addConverter(new StringToUserConverter());
        service.addConverter(new MapToUserConverter());
        
        return service;
    }
    
    // Custom converter example: String to User
    public static class StringToUserConverter implements Converter<String, User> {
        @Override
        public User convert(String source) {
            String[] parts = source.split(",");
            if (parts.length >= 2) {
                User user = new User();
                user.setName(parts[0]);
                user.setAge(Integer.parseInt(parts[1]));
                return user;
            }
            throw new IllegalArgumentException("Invalid user string format");
        }
    }
    
    // Custom converter example: Map to User
    public static class MapToUserConverter implements Converter<Map<String, Object>, User> {
        @Override
        public User convert(Map<String, Object> source) {
            User user = new User();
            if (source.get("name") != null) {
                user.setName(source.get("name").toString());
            }
            if (source.get("age") != null) {
                Object age = source.get("age");
                user.setAge(age instanceof Number ? ((Number) age).intValue() : Integer.parseInt(age.toString()));
            }
            return user;
        }
    }
}
