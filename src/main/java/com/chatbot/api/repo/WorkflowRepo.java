package com.chatbot.api.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.chatbot.api.models.Workflow;

public interface WorkflowRepo extends MongoRepository<Workflow, String> {
    Workflow findByName(String name);
}
