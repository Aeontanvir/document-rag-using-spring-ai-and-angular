package com.aeon.documentrag.backend.security;

public record AuthenticatedUser(
        String id,
        String email,
        String name
) {
}
