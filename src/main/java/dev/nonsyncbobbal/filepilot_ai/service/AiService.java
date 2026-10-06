package dev.nonsyncbobbal.filepilot_ai.service;

import dev.nonsyncbobbal.filepilot_ai.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;

    public String chat(String query, String userId, String chatId) {
        String conversationId = userId+":"+chatId;
        return chatClient
                .prompt()
                .user(query)
                .advisors(a -> a.param(
                                ChatMemory.CONVERSATION_ID,
                                conversationId))
                .call()
                .content();
    }
}
