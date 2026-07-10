package com.example.ai_chat_bot.Store;

import com.example.ai_chat_bot.Model.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class InMemoryConversationStore  implements ConversationStore{
    /**
     * Why ConcurrentHashMap? It makes our app thread-safe if multiple users chat at once.
     * Key: conversationId (A unique ID isolating one user's session from another).
     * Value: A LinkedList of messages representing their specific chat history.
     */
    private final ConcurrentHashMap<String, LinkedList<ChatMessage>> store = new ConcurrentHashMap<>();

    // We only keep the last 10 messages to save memory and API tokens
    private static final int MAX_HISTORY = 10;

    @Override
    public void addMessage(String conversationId, String role, String content) {
        // If this is a new conversation, create a new empty list.
        // If it exists, grab the current list.
        store.computeIfAbsent(conversationId, k -> new LinkedList<>()).add(new ChatMessage(role, content));

        // Enforce the 10-message limit by removing the oldest message at the front of the list
        LinkedList<ChatMessage> history = store.get(conversationId);
        if (history.size() > MAX_HISTORY) {
            history.removeFirst();
        }
    }

    @Override
    public List<ChatMessage> getHistory(String conversationId) {
        // Return the history, or an empty list if they haven't chatted yet
        return store.getOrDefault(conversationId, new LinkedList<>());
    }

    @Override
    public void clearConversation(String conversationId) {
        store.remove(conversationId);
    }
}
