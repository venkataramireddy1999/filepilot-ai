package dev.nonsyncbobbal.filepilot_ai.controller;

import dev.nonsyncbobbal.filepilot_ai.dto.ChatRequest;
import dev.nonsyncbobbal.filepilot_ai.service.AiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@Valid @RequestBody ChatRequest chatRequest) {
        System.out.println("Raw request: " + chatRequest.query());
        return ResponseEntity.ok(aiService.chat(chatRequest.query(),
                                            chatRequest.userId(),
                                            chatRequest.chatId()));
    }
}
