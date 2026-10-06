package dev.nonsyncbobbal.filepilot_ai.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "User ID cannot be empty")
        String userId,

        @NotBlank(message = "Chat ID cannot be empty")
        String chatId,

        @NotBlank(message= "Query cannot be empty")
        String query) {
}
