package com.example.ai_chat_bot.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public String greet() {
        return "Welcome to the AI Chat Bot! Your Spring Boot application is running successfully.";
    }
}
