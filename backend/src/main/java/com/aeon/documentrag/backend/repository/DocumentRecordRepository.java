package com.aeon.documentrag.backend.repository;

import com.aeon.documentrag.backend.entity.DocumentRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRecordRepository extends JpaRepository<DocumentRecordEntity, String> {

    List<DocumentRecordEntity> findAllByOrderByCreatedAtDesc();

    List<DocumentRecordEntity> findAllByProject_IdOrderByCreatedAtDesc(String projectId);

    List<DocumentRecordEntity> findAllByProject_Id(String projectId);

    Optional<DocumentRecordEntity> findByIdAndProject_Id(String documentId, String projectId);

    long countByProject_Id(String projectId);
}
