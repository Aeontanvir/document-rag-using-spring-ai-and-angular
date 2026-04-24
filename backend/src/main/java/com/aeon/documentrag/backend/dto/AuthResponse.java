package com.aeon.documentrag.backend.dto;

public record AuthResponse(
        String token,
        UserProfileResponse user
) {
}
