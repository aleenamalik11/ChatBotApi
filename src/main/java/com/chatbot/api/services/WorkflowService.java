package com.chatbot.api.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatbot.api.models.User;
import com.chatbot.api.models.Workflow;
import com.chatbot.api.repo.UserRepo;
import com.chatbot.api.repo.WorkflowRepo;

@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepo repository;

    public void saveWorkflow(Workflow workflow) {
        repository.save(workflow);
    }
    public Workflow getWorkflow(String id) {
        return repository.findById(id).orElse(null);
    }
    public List<Workflow> getAllWorkflows(){
        List<Workflow> workflos= repository.findAll();
        return workflos;
    }
    public Workflow getWorkflowByName(String name) {
        return repository.findByName(name);
    }
}
