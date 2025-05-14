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
@TypeAlias("custom_logic")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CustomNode extends WorkflowNode
{

	private String function;
	private Map<String, Object> inputs;
	
	@Override
	public String performExecution(Workflow workflow) {
		return "";
	}
}
