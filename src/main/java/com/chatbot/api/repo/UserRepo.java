package com.chatbot.api.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.chatbot.api.models.User;

public interface UserRepo extends MongoRepository<User, String> {
    List<User> findByAge(int age);
}
