package com.aeon.documentrag.backend.dto;

public record DocumentCitationResponse(
        String chunkId,
        String documentId,
        String sourceFileName,
        Integer chunkIndex,
        String excerpt
) {
}
