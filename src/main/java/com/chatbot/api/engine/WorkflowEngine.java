package com.chatbot.api.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatbot.api.models.Workflow;
import com.chatbot.api.services.WorkflowService;

@Service
public class WorkflowEngine {

	@Autowired
    private WorkflowService workflowService;
	
	public void StartEngine(String workflowName) {
        String currentNode = "start";
        Workflow workflow = workflowService.getWorkflowByName(workflowName);
                
        while(!currentNode.equals("done") || currentNode != null) {
        	WorkflowNode node = workflow.nodes.get(currentNode);
        	
        	if (node == null) throw new RuntimeException("Node not found: " + currentNode);
        	
        	String resultKey = node.performExecution(workflow);
        	
        	currentNode = node.getNextNode(currentNode, resultKey, workflow.connections);
        }
        
        
	}
	
	
}
