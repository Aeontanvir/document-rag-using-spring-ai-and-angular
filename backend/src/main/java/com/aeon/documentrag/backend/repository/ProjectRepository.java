package com.aeon.documentrag.backend.repository;

import com.aeon.documentrag.backend.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {

    List<ProjectEntity> findAllByOwner_IdOrderByCreatedAtDesc(String ownerId);

    Optional<ProjectEntity> findByIdAndOwner_Id(String projectId, String ownerId);
}
