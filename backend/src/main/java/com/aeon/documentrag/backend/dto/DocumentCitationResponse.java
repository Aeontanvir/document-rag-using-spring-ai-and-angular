package com.aeon.documentrag.backend.dto;

public record DocumentCitationResponse(
        String chunkId,
        String projectId,
        String documentId,
        String sourceFileName,
        Integer chunkIndex,
        String excerpt
) {
}
