package com.chatbot.api.models;

import java.util.Map;

import lombok.Getter;

@Getter
public class ConditionNode {

	private String expression;
	private Map<String, Object> context;
}
