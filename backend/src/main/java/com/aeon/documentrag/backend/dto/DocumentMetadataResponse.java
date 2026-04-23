package com.aeon.documentrag.backend.dto;

import java.time.Instant;

public record DocumentMetadataResponse(
        String id,
        String projectId,
        String projectName,
        String originalFilename,
        String mediaType,
        long sizeBytes,
        String checksum,
        int chunkCount,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
