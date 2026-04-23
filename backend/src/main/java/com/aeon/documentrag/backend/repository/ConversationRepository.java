package com.aeon.documentrag.backend.repository;

import com.aeon.documentrag.backend.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<ConversationEntity, String> {

    Optional<ConversationEntity> findByIdAndProject_Id(String conversationId, String projectId);

    List<ConversationEntity> findAllByProject_IdOrderByUpdatedAtDesc(String projectId);

    long countByProject_Id(String projectId);
}
