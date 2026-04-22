package com.aeon.documentrag.backend.repository;

import com.aeon.documentrag.backend.entity.ConversationMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessageEntity, Long> {

    List<ConversationMessageEntity> findByConversation_IdOrderByCreatedAtAsc(String conversationId);

    void deleteByConversation_Id(String conversationId);
}
