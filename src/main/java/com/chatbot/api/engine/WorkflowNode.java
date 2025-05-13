package com.chatbot.api.engine;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.chatbot.api.models.Workflow;
import com.chatbot.api.services.WorkflowService;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type"
	)
	@JsonSubTypes({
	    @JsonSubTypes.Type(value = MessageNode.class, name = "message"),
	    @JsonSubTypes.Type(value = CustomNode.class, name = "custom_logic"),
	    @JsonSubTypes.Type(value = ConditionNode.class, name = "condition"),
	    @JsonSubTypes.Type(value = InputNode.class, name = "input") // Add this for your input type
	})
public abstract class WorkflowNode {
	
	public abstract String performExecution(Workflow workflow);
	
	public String getNextNode(String node, String resultKey, Map<String, Map<String, String>> connections) {
		
		return connections.get(node).get(resultKey);
	}
}
