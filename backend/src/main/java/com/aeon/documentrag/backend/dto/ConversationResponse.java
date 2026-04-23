package com.aeon.documentrag.backend.dto;

import java.time.Instant;
import java.util.List;

public record ConversationResponse(
        String conversationId,
        String projectId,
        String projectName,
        String title,
        Instant createdAt,
        Instant updatedAt,
        List<ConversationMessageResponse> messages
) {
}
