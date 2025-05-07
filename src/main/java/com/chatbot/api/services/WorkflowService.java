package com.chatbot.api.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatbot.api.models.User;
import com.chatbot.api.repo.UserRepo;

@Service
public class WorkflowService {

    @Autowired
    private UserRepo repository;

    public void saveUser(User user) {
        repository.save(user);
    }
    public User getUser(String id) {
        return repository.findById(id).orElse(null);
    }
    public List<User> getAllUsers(){
        return repository.findAll();
    }
    public void deleteUser(String id) {
        repository.deleteById(id);
    }
}
