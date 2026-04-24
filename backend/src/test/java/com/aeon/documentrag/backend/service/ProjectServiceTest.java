package com.aeon.documentrag.backend.service;

import com.aeon.documentrag.backend.dto.ProjectCreateRequest;
import com.aeon.documentrag.backend.dto.ProjectResponse;
import com.aeon.documentrag.backend.entity.DocumentRecordEntity;
import com.aeon.documentrag.backend.entity.ProjectEntity;
import com.aeon.documentrag.backend.entity.UserEntity;
import com.aeon.documentrag.backend.repository.ConversationMessageRepository;
import com.aeon.documentrag.backend.repository.ConversationRepository;
import com.aeon.documentrag.backend.repository.DocumentRecordRepository;
import com.aeon.documentrag.backend.repository.ProjectRepository;
import com.aeon.documentrag.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.VectorStore;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private DocumentRecordRepository documentRecordRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMessageRepository conversationMessageRepository;

    @Mock
    private DocumentChunkingService documentChunkingService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private VectorStore vectorStore;

    @Mock
    private UserRepository userRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(
                projectRepository,
                documentRecordRepository,
                conversationRepository,
                conversationMessageRepository,
                documentChunkingService,
                fileStorageService,
                vectorStore,
                userRepository
        );
    }

    @Test
    void createProjectShouldTrimFieldsBeforeSaving() {
        Instant now = Instant.parse("2026-04-23T10:15:30Z");
        UserEntity owner = new UserEntity();
        owner.setId("user-1");
        when(userRepository.findById("user-1")).thenReturn(java.util.Optional.of(owner));
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(invocation -> {
            ProjectEntity entity = invocation.getArgument(0);
            entity.setId("project-1");
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            return entity;
        });

        ProjectResponse response = projectService.createProject("user-1", new ProjectCreateRequest("  Knowledge Hub  ", "  Docs and notes  "));

        ArgumentCaptor<ProjectEntity> entityCaptor = ArgumentCaptor.forClass(ProjectEntity.class);
        verify(projectRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getName()).isEqualTo("Knowledge Hub");
        assertThat(entityCaptor.getValue().getDescription()).isEqualTo("Docs and notes");
        assertThat(entityCaptor.getValue().getOwner()).isSameAs(owner);
        assertThat(response.name()).isEqualTo("Knowledge Hub");
        assertThat(response.description()).isEqualTo("Docs and notes");
    }

    @Test
    void createProjectShouldRejectBlankNames() {
        assertThatThrownBy(() -> projectService.createProject("user-1", new ProjectCreateRequest("   ", "ignored")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project name is required");
    }

    @Test
    void deleteProjectShouldRemoveStoredFilesConversationDataAndChunks() {
        ProjectEntity project = new ProjectEntity();
        project.setId("project-123");

        DocumentRecordEntity firstDocument = new DocumentRecordEntity();
        firstDocument.setId("doc-1");
        firstDocument.setChunkCount(2);
        firstDocument.setStoragePath("/tmp/doc-1.pdf");

        DocumentRecordEntity secondDocument = new DocumentRecordEntity();
        secondDocument.setId("doc-2");
        secondDocument.setChunkCount(0);
        secondDocument.setStoragePath("/tmp/doc-2.txt");

        when(projectRepository.findByIdAndOwner_Id("project-123", "user-1")).thenReturn(java.util.Optional.of(project));
        when(documentRecordRepository.findAllByProject_Id("project-123")).thenReturn(List.of(firstDocument, secondDocument));
        when(conversationRepository.findAllByProject_IdOrderByUpdatedAtDesc("project-123")).thenReturn(List.of());
        when(documentChunkingService.buildChunkId("doc-1", 1)).thenReturn("doc-1-chunk-1");
        when(documentChunkingService.buildChunkId("doc-1", 2)).thenReturn("doc-1-chunk-2");

        projectService.deleteProject("user-1", "project-123");

        verify(vectorStore).delete(List.of("doc-1-chunk-1", "doc-1-chunk-2"));
        verify(fileStorageService).deleteIfExists("/tmp/doc-1.pdf");
        verify(fileStorageService).deleteIfExists("/tmp/doc-2.txt");
        verify(documentRecordRepository).deleteAll(List.of(firstDocument, secondDocument));
        verify(projectRepository).delete(project);
        verify(vectorStore, never()).delete(List.of());
    }
}
