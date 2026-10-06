package dev.nonsyncbobbal.filepilot_ai.service;

import dev.nonsyncbobbal.filepilot_ai.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;

    public String chat(String query) {
        return chatClient
                .prompt()
                .user(query)
                .call()
                .content();
    }
}
