package com.chatbot.api.interfaces;

import com.chatbot.api.models.ConditionNode;
import com.chatbot.api.models.CustomNode;
import com.chatbot.api.models.InputNode;
import com.chatbot.api.models.MessageNode;
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
public interface WorkflowNode {

}
