package dev.nonsyncbobbal.filepilot_ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message= "Query cannot be empty")
        String query) {
}
