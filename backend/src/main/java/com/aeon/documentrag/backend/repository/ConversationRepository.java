package com.aeon.documentrag.backend.repository;

import com.aeon.documentrag.backend.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<ConversationEntity, String> {
}
