package com.chatbot.api.models;

import java.util.Map;

import com.chatbot.api.interfaces.WorkflowNode;

import lombok.Getter;

@Getter
public class ConditionNode implements WorkflowNode
{

	private String expression;
	private Map<String, Object> context;
}
