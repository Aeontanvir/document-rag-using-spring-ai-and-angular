package com.aeon.documentrag.backend.controller;

import com.aeon.documentrag.backend.dto.ProjectCreateRequest;
import com.aeon.documentrag.backend.dto.ProjectResponse;
import com.aeon.documentrag.backend.security.AuthenticatedUser;
import com.aeon.documentrag.backend.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Project management endpoints for project-scoped RAG")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a project")
    public ResponseEntity<ProjectResponse> createProject(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                         @Valid @RequestBody ProjectCreateRequest request) {
        return ResponseEntity.ok(projectService.createProject(authenticatedUser.id(), request));
    }

    @GetMapping
    @Operation(summary = "List projects")
    public ResponseEntity<List<ProjectResponse>> listProjects(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(projectService.listProjects(authenticatedUser.id()));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Get project details")
    public ResponseEntity<ProjectResponse> getProject(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                      @PathVariable String projectId) {
        return ResponseEntity.ok(projectService.getProject(authenticatedUser.id(), projectId));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete a project with its documents and conversations")
    public ResponseEntity<Void> deleteProject(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                              @PathVariable String projectId) {
        projectService.deleteProject(authenticatedUser.id(), projectId);
        return ResponseEntity.noContent().build();
    }
}
