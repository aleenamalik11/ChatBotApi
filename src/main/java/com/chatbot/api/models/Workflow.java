package com.chatbot.api.models;

import java.util.List;
import java.util.Map;

import com.chatbot.api.interfaces.WorkflowNode;

public class Workflow {
	public String name;
    public String version;
    public List<String> inputVariables;

    public Map<String, WorkflowNode> nodes; 
    public Map<String, Map<String, String>> connections; 
}
