package com.aeon.documentrag.backend.repository;

import com.aeon.documentrag.backend.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {

    List<ProjectEntity> findAllByOrderByCreatedAtDesc();
}
