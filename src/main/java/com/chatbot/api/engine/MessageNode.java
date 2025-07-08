package com.chatbot.api.engine;

import org.springframework.data.annotation.TypeAlias;

import com.chatbot.api.models.Workflow;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("message")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class MessageNode extends WorkflowNode
{
	private String message;
	
	@Override
	public String performExecution(Workflow workflow) {
		if(message != null) {
			System.out.println(message);
			return "success";
		}
		else return "failure";
	}
}
