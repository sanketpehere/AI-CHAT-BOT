package com.example.ai_chat_bot.Controller;

import com.example.ai_chat_bot.Model.ChatMessage;
import com.example.ai_chat_bot.Service.ChatService;
import com.example.ai_chat_bot.Service.ConversationChatService;
import com.example.ai_chat_bot.Store.ConversationStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService; // Old stateless service
    private final ConversationChatService conversationChatService; // New stateful service
    private final ConversationStore conversationStore;

    public ChatController(ChatService chatService, ConversationChatService conversationChatService, ConversationStore conversationStore) {
        this.chatService = chatService;
        this.conversationChatService = conversationChatService;
        this.conversationStore = conversationStore;
    }

    // --- STATELESS ENDPOINTS ---

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChatStateless(@RequestParam String question) {
        return chatService.streamChat(question);
    }

    @PostMapping("/stateless")
    public Flux<String> postChatStateless(@RequestParam String question) {
        return chatService.streamChat(question);
    }

    // --- STATEFUL ENDPOINTS ---

    @GetMapping(value = "/conversation/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChatStateful(@RequestParam String conversationId, @RequestParam String question) {
        return conversationChatService.streamStatefulChat(conversationId, question);
    }

    @PostMapping("/conversation")
    public Flux<String> postChatStateful(@RequestParam String conversationId, @RequestParam String question) {
        return conversationChatService.streamStatefulChat(conversationId, question);
    }

    @GetMapping("/conversation/history")
    public List<ChatMessage> getHistory(@RequestParam String conversationId) {
        return conversationStore.getHistory(conversationId);
    }

    @DeleteMapping("/conversation")
    public String deleteHistory(@RequestParam String conversationId) {
        conversationStore.clearConversation(conversationId);
        return "Conversation " + conversationId + " cleared!";
    }
}
