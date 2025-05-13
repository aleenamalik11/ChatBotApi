package com.chatbot.api.engine;

import org.springframework.beans.factory.annotation.Autowired;

import com.chatbot.api.models.Workflow;
import com.chatbot.api.services.WorkflowService;

public class WorkflowEngine {

	@Autowired
    private WorkflowService workflowService;
	
	public void StartEngine(String workflowName) {
        String currentNode = "start";
        Workflow workflow = workflowService.getWorkflowByName(workflowName);
                
        while(currentNode != null) {
        	WorkflowNode node = workflow.nodes.get(currentNode);
        	
        	if (node == null) throw new RuntimeException("Node not found: " + currentNode);
        	
        	String resultKey = node.PerformExecution(workflow);
        	
        	currentNode = node.getNextNode(currentNode, resultKey, workflow.connections);
        }
        
        
	}
	
	
}
