package com.chatbot.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class ChatbotController {

	@GetMapping("/")
    @Operation(summary = "Welcome Message")
	public String index() {
		return "Greetings from ChatBotApi!";
	}
}
