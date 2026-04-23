package com.aeon.documentrag.backend.dto;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        String conversationId,
        String projectId,
        String answer,
        List<DocumentCitationResponse> citations,
        Instant respondedAt
) {
}
