package com.chatbot.api.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoConfig {
 @Bean
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory factory, 
                                                     MongoMappingContext context, 
                                                     BeanFactory beanFactory) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
        mappingConverter.setCustomConversions(mongoCustomConversions());
        
        // This is important for polymorphic types
        mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null, context));
        
        return mappingConverter;
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
    	List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new WorkflowNodeReadConverter());
        return new MongoCustomConversions(converters);
    }
}
