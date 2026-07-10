package com.example.ai_chat_bot.Service;

import com.example.ai_chat_bot.Model.ChatMessage;
import com.example.ai_chat_bot.Store.ConversationStore;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;
import com.openai.models.responses.ResponseCreateParams;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
@Service
public class ConversationChatService {
    private final OpenAIClient openAIClient;
    private final CostGuard costGuard;
    private final ConversationStore conversationStore;

    public ConversationChatService(OpenAIClient openAIClient, CostGuard costGuard, ConversationStore conversationStore) {
        this.openAIClient = openAIClient;
        this.costGuard = costGuard;
        this.conversationStore = conversationStore;
    }

    public Flux<String> streamStatefulChat(String conversationId, String userMessage) {
        // 1. Validate the input
        costGuard.validate(userMessage);

        // 2. Save the new user message to memory
        conversationStore.addMessage(conversationId, "user", userMessage);

        // 3. Load the full history (max 10 messages)
        List<ChatMessage> history = conversationStore.getHistory(conversationId);

        // 4. Map custom ChatMessage to the standard Chat Completions format
        List<ChatCompletionMessageParam> openAiMessages = new ArrayList<>();

        // Add the System Prompt using the strict builder
        openAiMessages.add(ChatCompletionMessageParam.ofSystem(
                ChatCompletionSystemMessageParam.builder()
                        .content("You are a helpful, beginner-friendly AI assistant.")
                        .build()
        ));

        // Add the rest of the conversation history using the strict builders
        for (ChatMessage msg : history) {
            if (msg.role().equals("user")) {
                openAiMessages.add(ChatCompletionMessageParam.ofUser(
                        ChatCompletionUserMessageParam.builder()
                                .content(msg.content())
                                .build()
                ));
            } else {
                openAiMessages.add(ChatCompletionMessageParam.ofAssistant(
                        ChatCompletionAssistantMessageParam.builder()
                                .content(msg.content())
                                .build()
                ));
            }
        }

        return Flux.<String>create(sink -> {
            try {
                // 5. Send full history via Chat Completions API (Gemini compatible)
                ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                        .model("gemini-2.5-flash") // Using the free Gemini model
                        .messages(openAiMessages)
                        .build();

                StringBuilder assistantFullReply = new StringBuilder();

                // 6. Stream answer using Flux
                try (var stream = openAIClient.chat().completions().createStreaming(params)) {
                    stream.stream()
                            .forEach(chunk -> {
                                chunk.choices().stream()
                                        .findFirst()
                                        .map(choice -> choice.delta().content())
                                        .filter(java.util.Optional::isPresent)
                                        .map(java.util.Optional::get)
                                        .ifPresent(text -> {
                                            assistantFullReply.append(text);
                                            sink.next(text);
                                        });
                            });
                }

                // 7. Save assistant reply to memory AFTER the stream completes successfully
                conversationStore.addMessage(conversationId, "assistant", assistantFullReply.toString());
                sink.complete();

            } catch (Exception e) {
                sink.error(new RuntimeException("Failed to reach AI: " + e.getMessage()));
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
