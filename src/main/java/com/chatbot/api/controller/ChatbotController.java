package com.chatbot.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatbotController {

	@GetMapping("/")
	public String index() {
		return "Greetings from ChatBotApi!";
	}
}
