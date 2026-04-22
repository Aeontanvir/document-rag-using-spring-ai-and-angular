package com.aeon.documentrag.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        String conversationId,
        @NotBlank(message = "Prompt is required")
        String prompt,
        @Min(value = 1, message = "topK must be at least 1")
        @Max(value = 10, message = "topK cannot exceed 10")
        Integer topK
) {
}
