package com.aeon.documentrag.backend.mapper;

import com.aeon.documentrag.backend.dto.DocumentMetadataResponse;
import com.aeon.documentrag.backend.entity.DocumentRecordEntity;

public final class DocumentMapper {

    private DocumentMapper() {
    }

    public static DocumentMetadataResponse toResponse(DocumentRecordEntity entity) {
        return new DocumentMetadataResponse(
                entity.getId(),
                entity.getOriginalFilename(),
                entity.getMediaType(),
                entity.getSizeBytes(),
                entity.getChecksum(),
                entity.getChunkCount(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
