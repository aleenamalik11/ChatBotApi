package com.chatbot.api.engine;

import org.springframework.data.annotation.TypeAlias;

import com.chatbot.api.models.Workflow;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("input")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class InputNode extends WorkflowNode {
    private String prompt;
    
    @Override
	public String performExecution(Workflow workflow) {
    	return "";		
	}
}
