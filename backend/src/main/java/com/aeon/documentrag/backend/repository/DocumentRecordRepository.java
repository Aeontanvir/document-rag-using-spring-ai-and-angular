package com.aeon.documentrag.backend.repository;

import com.aeon.documentrag.backend.entity.DocumentRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRecordRepository extends JpaRepository<DocumentRecordEntity, String> {

    List<DocumentRecordEntity> findAllByOrderByCreatedAtDesc();
}
