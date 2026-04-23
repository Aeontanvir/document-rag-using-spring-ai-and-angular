package com.aeon.documentrag.backend.mapper;

import com.aeon.documentrag.backend.dto.ProjectResponse;
import com.aeon.documentrag.backend.entity.ProjectEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProjectMapper {

    public static ProjectResponse toResponse(ProjectEntity entity, long documentCount, long conversationCount) {
        return new ProjectResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                documentCount,
                conversationCount,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
