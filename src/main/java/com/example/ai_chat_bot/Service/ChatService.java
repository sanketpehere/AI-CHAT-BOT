package com.example.ai_chat_bot.Service;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {
    @Value("${app.mock-mode:false}")
    private boolean mockMode;

    private final OpenAIClient openAIClient;
    private final CostGuard costGuard;

    // Local cache for repeated questions
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public ChatService(OpenAIClient openAIClient, CostGuard costGuard) {
        this.openAIClient = openAIClient;
        this.costGuard = costGuard;
    }

    public Flux<String> streamChat(String userMessage) {
        // 1. Guard against large/expensive inputs
        costGuard.validate(userMessage);

        // 2. Return cached response if this exact question was asked before
        if (cache.containsKey(userMessage)) {
            return Flux.just(cache.get(userMessage));
        }

        // 3. Handle Mock Mode (no API key required)
        if (mockMode) {
            String mockAnswer = "This is a simulated mock response for: " + userMessage;
            cache.put(userMessage, mockAnswer);
            return Flux.fromArray(mockAnswer.split(" "))
                    .map(word -> word + " ")
                    .delayElements(Duration.ofMillis(100));
        }

        // 4. Call Gemini using the compatible Chat Completions API
        return Flux.<String>create(sink -> {
            try {
                // Using the corrected package and the cleaner .addUserMessage() method
                ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                        .model("gemini-2.5-flash")
                        .addUserMessage(userMessage)
                        .build();

                StringBuilder fullResponse = new StringBuilder();

                // Stream the response from Google's servers
                try (var stream = openAIClient.chat().completions().createStreaming(params)) {
                    stream.stream()
                            .forEach(chunk -> {
                                // Extract the text from the chat completion chunk safely
                                chunk.choices().stream()
                                        .findFirst()
                                        .map(choice -> choice.delta().content())
                                        .filter(java.util.Optional::isPresent)
                                        .map(java.util.Optional::get)
                                        .ifPresent(text -> {
                                            fullResponse.append(text);
                                            sink.next(text); // Send piece to the client immediately
                                        });
                            });
                }

                cache.put(userMessage, fullResponse.toString());
                sink.complete();

            } catch (Exception e) {
                sink.error(new RuntimeException("Failed to reach AI: " + e.getMessage()));
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
