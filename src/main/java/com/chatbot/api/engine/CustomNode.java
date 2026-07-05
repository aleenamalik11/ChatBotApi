package com.chatbot.api.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.annotation.TypeAlias;

import com.chatbot.api.dto.MethodDetails;
import com.chatbot.api.dto.MethodResult;
import com.chatbot.api.models.Workflow;
import com.chatbot.api.utils.BeanResolver;
import com.chatbot.api.utils.MethodInvoker;
import com.chatbot.api.utils.SpringBeanProvider;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("custom_logic")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CustomNode extends WorkflowNode {
    
    private String function;
    private Object output;
    
    private final BeanResolver beanResolver;

    public CustomNode(BeanResolver beanResolver) {
        this.beanResolver = beanResolver;
    }
    
    @Override
    public String performExecution(Workflow workflow) {
    	MethodInvoker methodInvoker = (MethodInvoker) beanResolver.getBean("MethodInvoker");
    	
    	MethodResult result =  methodInvoker.invoke(workflow.inputs, function);
    	
    	output = result.getOutput();
    	
    	return result.getResult();
    }
}