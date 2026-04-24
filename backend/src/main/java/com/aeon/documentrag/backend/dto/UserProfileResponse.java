package com.aeon.documentrag.backend.dto;

import java.time.Instant;

public record UserProfileResponse(
        String id,
        String name,
        String email,
        Instant createdAt
) {
}
