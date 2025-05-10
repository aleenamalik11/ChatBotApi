package com.chatbot.api.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import com.chatbot.api.interfaces.WorkflowNode;

@ReadingConverter
public class WorkflowNodeReadConverter implements Converter<Document, WorkflowNode> {
    @Override
    public WorkflowNode convert(Document source) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(source, WorkflowNode.class);
    }
}
