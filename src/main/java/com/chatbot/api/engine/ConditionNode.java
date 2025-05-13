package com.chatbot.api.engine;

import java.util.Map;

import org.springframework.data.annotation.TypeAlias;

import com.chatbot.api.models.Workflow;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Getter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("condition")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ConditionNode extends WorkflowNode
{

	private String expression;
	private Map<String, Object> inputs;
	
	@Override
	public String performExecution(Workflow workflow) {
		return "";		
	}
}
