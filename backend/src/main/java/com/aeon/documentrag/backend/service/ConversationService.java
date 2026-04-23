package com.aeon.documentrag.backend.service;

import com.aeon.documentrag.backend.dto.ConversationResponse;
import com.aeon.documentrag.backend.entity.ConversationEntity;
import com.aeon.documentrag.backend.entity.ConversationMessageEntity;
import com.aeon.documentrag.backend.entity.ProjectEntity;
import com.aeon.documentrag.backend.entity.type.ConversationRole;
import com.aeon.documentrag.backend.exception.ResourceNotFoundException;
import com.aeon.documentrag.backend.mapper.ConversationMapper;
import com.aeon.documentrag.backend.repository.ConversationMessageRepository;
import com.aeon.documentrag.backend.repository.ConversationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;

    @Transactional
    public String ensureConversation(String requestedConversationId, ProjectEntity project, String seedPrompt) {
        if (requestedConversationId != null && !requestedConversationId.isBlank()) {
            return conversationRepository.findByIdAndProject_Id(requestedConversationId, project.getId())
                    .map(ConversationEntity::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + requestedConversationId));
        }

        ConversationEntity conversation = new ConversationEntity();
        conversation.setTitle(buildTitle(seedPrompt));
        conversation.setProject(project);
        return conversationRepository.save(conversation).getId();
    }

    @Transactional
    public void appendMessage(String conversationId, ConversationRole role, String content) {
        ConversationEntity conversation = getConversationEntity(conversationId);
        conversation.touch();
        ConversationMessageEntity message = new ConversationMessageEntity();
        message.setConversation(conversation);
        message.setRole(role);
        message.setContent(content);
        conversationMessageRepository.save(message);
        conversationRepository.save(conversation);
    }

    public String renderConversationHistory(String conversationId, int limit) {
        List<ConversationMessageEntity> messages = conversationMessageRepository.findByConversation_IdOrderByCreatedAtAsc(conversationId);
        int start = Math.max(messages.size() - limit, 0);
        return messages.subList(start, messages.size())
                .stream()
                .map(message -> message.getRole().name() + ": " + message.getContent())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    public ConversationResponse getConversation(String projectId, String conversationId) {
        ConversationEntity conversation = getConversationEntity(projectId, conversationId);
        List<ConversationMessageEntity> messages = conversationMessageRepository.findByConversation_IdOrderByCreatedAtAsc(conversationId);
        return ConversationMapper.toResponse(conversation, messages);
    }

    @Transactional
    public void deleteConversation(String projectId, String conversationId) {
        getConversationEntity(projectId, conversationId);
        conversationMessageRepository.deleteByConversation_Id(conversationId);
        ConversationEntity conversation = getConversationEntity(projectId, conversationId);
        conversationRepository.delete(conversation);
    }

    private ConversationEntity getConversationEntity(String projectId, String conversationId) {
        return conversationRepository.findByIdAndProject_Id(conversationId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found in project: " + conversationId));
    }

    private ConversationEntity getConversationEntity(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));
    }

    private String buildTitle(String prompt) {
        String normalized = prompt == null ? "New conversation" : prompt.trim();
        if (normalized.isBlank()) {
            return "New conversation";
        }
        return normalized.length() > 60 ? normalized.substring(0, 57) + "..." : normalized;
    }
}
