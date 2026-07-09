package com.example.ai_chat_bot.Service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class CostGuard {
    // Limit inputs to roughly 100 words
    private static final int MAX_CHARS = 500;

    public void validate(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be empty.");
        }
        if (input.length() > MAX_CHARS) {
            throw new IllegalArgumentException("Input too long! Max limit is " + MAX_CHARS + " characters to save costs.");
        }
    }
}
