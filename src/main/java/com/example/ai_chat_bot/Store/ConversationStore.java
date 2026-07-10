package com.example.ai_chat_bot.Store;

import com.example.ai_chat_bot.Model.ChatMessage;

import java.util.List;

public interface ConversationStore {
    void addMessage(String conversationId, String role, String content);
    List<ChatMessage> getHistory(String conversationId);
    void clearConversation(String conversationId);
}
