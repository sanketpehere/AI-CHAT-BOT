package com.example.ai_chat_bot.Configuration;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {
    @Value("${app.mock-mode:false}")
    private boolean mockMode;

    @Value("${OPENAI_API_KEY:}")
    private String apiKey;

    @Bean
    public OpenAIClient openAIClient() {
        if (mockMode || apiKey.isBlank()) {
            // Provide a dummy key so the app doesn't crash on startup in mock mode
            return OpenAIOkHttpClient.builder().apiKey("mock-key").build();
        }

        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                // Add this line to point the OpenAI SDK at Gemini!
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/openai/")
                .build();
    }
}
