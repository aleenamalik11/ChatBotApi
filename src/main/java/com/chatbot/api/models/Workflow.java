package com.chatbot.api.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import com.chatbot.api.engine.WorkflowNode;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "workflow", description = "A workflow in the system")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Workflow {
	@Schema(description = "Workflow ID", example = "abc123")
	private String id;
	@Schema(description = "name", example = "abc123")
	public String name;
	@Schema(description = "version", example = "abc123")
    public String version;
	@Schema(description = "input vars")
    public List<WorkflowInput> inputVariables;
	@Schema(description = "nodes")
	@JsonDeserialize(contentAs = WorkflowNode.class)
	@Field(targetType = FieldType.IMPLICIT) 
    public Map<String, WorkflowNode> nodes; 
	@Schema(description = "connections")
    public Map<String, Map<String, String>> connections; 	
	public Map<String, Object> inputs = new HashMap<>();
}
