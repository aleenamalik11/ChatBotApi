package com.chatbot.api.models;

import java.util.Map;

import org.springframework.data.annotation.TypeAlias;

import com.chatbot.api.interfaces.WorkflowNode;
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
public class ConditionNode implements WorkflowNode
{

	private String expression;
	private Map<String, Object> inputs;
}
