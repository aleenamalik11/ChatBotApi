package com.chatbot.customservices;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatbot.api.models.User;
import com.chatbot.api.repo.UserRepo;

@Service
public class UserService {

    @Autowired
    private UserRepo repository;

    public void saveUser(Map<String, Object> inputs) {
    	User user = new User();

        if (inputs.get("name") != null) {
            user.setName(inputs.get("name").toString());
        }

        if (inputs.get("age") != null) {
            Object ageObj = inputs.get("age");
            int age = (ageObj instanceof Number)
                ? ((Number) ageObj).intValue()
                : Integer.parseInt(ageObj.toString());
            user.setAge(age);
        }
        
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
