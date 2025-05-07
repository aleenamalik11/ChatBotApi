package com.chatbot.api.models;

import com.chatbot.api.interfaces.WorkflowNode;

import lombok.Getter;

@Getter
public class CustomNode implements WorkflowNode
{

	private String function;
}
