package com.chatbot.api.engine;

import org.springframework.data.annotation.TypeAlias;

import com.chatbot.api.dto.MethodResult;
import com.chatbot.api.helperservices.MethodInvoker;
import com.chatbot.api.models.Workflow;
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
    
    @Override
    public String performExecution(Workflow workflow) {
    	MethodInvoker methodInvoker = SpringBeanProvider.getMethodInvoker();
    	
    	MethodResult result =  methodInvoker.invoke(workflow.inputs, function, "com.chatbot.customservices");
    	
    	output = result.getOutput();
    	
    	return result.getResult();
    }
}