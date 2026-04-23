package com.aeon.documentrag.backend.mapper;

import com.aeon.documentrag.backend.dto.DocumentMetadataResponse;
import com.aeon.documentrag.backend.entity.DocumentRecordEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DocumentMapper {

    public static DocumentMetadataResponse toResponse(DocumentRecordEntity entity) {
        return new DocumentMetadataResponse(
                entity.getId(),
                entity.getProject().getId(),
                entity.getProject().getName(),
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
