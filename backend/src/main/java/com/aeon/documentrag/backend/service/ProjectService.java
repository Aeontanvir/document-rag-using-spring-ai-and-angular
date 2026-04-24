package com.aeon.documentrag.backend.service;

import com.aeon.documentrag.backend.dto.ProjectCreateRequest;
import com.aeon.documentrag.backend.dto.ProjectResponse;
import com.aeon.documentrag.backend.entity.ConversationEntity;
import com.aeon.documentrag.backend.entity.DocumentRecordEntity;
import com.aeon.documentrag.backend.entity.ProjectEntity;
import com.aeon.documentrag.backend.entity.UserEntity;
import com.aeon.documentrag.backend.exception.ResourceNotFoundException;
import com.aeon.documentrag.backend.mapper.ProjectMapper;
import com.aeon.documentrag.backend.repository.ConversationMessageRepository;
import com.aeon.documentrag.backend.repository.ConversationRepository;
import com.aeon.documentrag.backend.repository.DocumentRecordRepository;
import com.aeon.documentrag.backend.repository.ProjectRepository;
import com.aeon.documentrag.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final DocumentRecordRepository documentRecordRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final DocumentChunkingService documentChunkingService;
    private final FileStorageService fileStorageService;
    private final VectorStore vectorStore;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse createProject(String ownerId, ProjectCreateRequest request) {
        String normalizedName = request.name() == null ? "" : request.name().trim();
        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("Project name is required");
        }

        UserEntity owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerId));

        ProjectEntity entity = new ProjectEntity();
        entity.setName(normalizedName);
        entity.setOwner(owner);
        entity.setDescription(request.description() == null || request.description().isBlank() ? null : request.description().trim());
        return ProjectMapper.toResponse(projectRepository.save(entity), 0, 0);
    }

    public List<ProjectResponse> listProjects(String ownerId) {
        return projectRepository.findAllByOwner_IdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getProject(String ownerId, String projectId) {
        return toResponse(getProjectEntity(ownerId, projectId));
    }

    @Transactional
    public void deleteProject(String ownerId, String projectId) {
        ProjectEntity project = getProjectEntity(ownerId, projectId);

        List<DocumentRecordEntity> documents = documentRecordRepository.findAllByProject_Id(projectId);
        for (DocumentRecordEntity document : documents) {
            List<String> chunkIds = IntStream.rangeClosed(1, document.getChunkCount())
                    .mapToObj(index -> documentChunkingService.buildChunkId(document.getId(), index))
                    .toList();
            if (!chunkIds.isEmpty()) {
                vectorStore.delete(chunkIds);
            }
            fileStorageService.deleteIfExists(document.getStoragePath());
        }
        documentRecordRepository.deleteAll(documents);

        List<ConversationEntity> conversations = conversationRepository.findAllByProject_IdOrderByUpdatedAtDesc(projectId);
        for (ConversationEntity conversation : conversations) {
            conversationMessageRepository.deleteByConversation_Id(conversation.getId());
        }
        conversationRepository.deleteAll(conversations);
        projectRepository.delete(project);
    }

    public ProjectEntity getProjectEntity(String ownerId, String projectId) {
        return projectRepository.findByIdAndOwner_Id(projectId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    private ProjectResponse toResponse(ProjectEntity entity) {
        return ProjectMapper.toResponse(
                entity,
                documentRecordRepository.countByProject_Id(entity.getId()),
                conversationRepository.countByProject_Id(entity.getId())
        );
    }
}
