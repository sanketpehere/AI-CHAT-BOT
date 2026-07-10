package com.example.ai_chat_bot.Controller;

import com.example.ai_chat_bot.Service.ChatService;
import com.example.ai_chat_bot.Service.ConversationChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/demo")
public class DemoController {
    private final ChatService chatService;
    private final ConversationChatService conversationChatService;

    public DemoController(ChatService chatService, ConversationChatService conversationChatService) {
        this.chatService = chatService;
        this.conversationChatService = conversationChatService;
    }

    @GetMapping(value = "/stateless", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> demoStateless(@RequestParam String question) {
        return chatService.streamChat(question);
    }

    @GetMapping(value = "/stateful", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> demoStateful(@RequestParam String conversationId, @RequestParam String question) {
        return conversationChatService.streamStatefulChat(conversationId, question);
    }
}
