package com.chatbot.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.api.engine.WorkflowEngine;
import com.chatbot.api.models.User;
import com.chatbot.api.models.Workflow;
import com.chatbot.api.services.UserService;
import com.chatbot.api.services.WorkflowService;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
	
	@Autowired
    private WorkflowService workflowService;
	
	@Autowired
    private WorkflowEngine workflowEngine;

    @GetMapping
    public List<Workflow> getAllWorkflows() {
        return workflowService.getAllWorkflows();
    }

    @GetMapping("/{name}")
    public Workflow getWorkflowByName(@PathVariable String name) {
        return workflowService.getWorkflowByName(name);
    }

    @PostMapping
    public void createWorkflow(@RequestBody Workflow workflow) {
        workflowService.saveWorkflow(workflow);
    }
    
    @PostMapping("/{name}")
    public void startWorkflow(@PathVariable String name) {
    	workflowEngine.StartEngine(name);
    }
}
