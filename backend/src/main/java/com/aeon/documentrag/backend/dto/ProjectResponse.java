package com.aeon.documentrag.backend.dto;

import java.time.Instant;

public record ProjectResponse(
        String id,
        String name,
        String description,
        long documentCount,
        long conversationCount,
        Instant createdAt,
        Instant updatedAt
) {
}
