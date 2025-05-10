package com.chatbot.api.models;

import org.springframework.data.annotation.TypeAlias;

import com.chatbot.api.interfaces.WorkflowNode;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("message")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class MessageNode implements WorkflowNode
{

	private String message;
}
