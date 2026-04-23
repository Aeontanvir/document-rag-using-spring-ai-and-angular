package com.aeon.documentrag.backend.mapper;

import com.aeon.documentrag.backend.dto.ConversationMessageResponse;
import com.aeon.documentrag.backend.dto.ConversationResponse;
import com.aeon.documentrag.backend.entity.ConversationEntity;
import com.aeon.documentrag.backend.entity.ConversationMessageEntity;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ConversationMapper {

    public static ConversationResponse toResponse(ConversationEntity entity, List<ConversationMessageEntity> messages) {
        return new ConversationResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                messages.stream()
                        .map(ConversationMapper::toMessageResponse)
                        .toList()
        );
    }

    public static ConversationMessageResponse toMessageResponse(ConversationMessageEntity entity) {
        return new ConversationMessageResponse(
                entity.getId(),
                entity.getRole().name(),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }
}
