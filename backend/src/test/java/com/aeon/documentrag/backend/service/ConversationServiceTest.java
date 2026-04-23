package com.aeon.documentrag.backend.service;

import com.aeon.documentrag.backend.dto.ConversationResponse;
import com.aeon.documentrag.backend.entity.ConversationEntity;
import com.aeon.documentrag.backend.entity.ConversationMessageEntity;
import com.aeon.documentrag.backend.entity.ProjectEntity;
import com.aeon.documentrag.backend.entity.type.ConversationRole;
import com.aeon.documentrag.backend.repository.ConversationMessageRepository;
import com.aeon.documentrag.backend.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMessageRepository conversationMessageRepository;

    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        conversationService = new ConversationService(conversationRepository, conversationMessageRepository);
    }

    @Test
    void ensureConversationShouldCreateNewConversationWithTrimmedTitle() {
        ProjectEntity project = new ProjectEntity();
        project.setId("project-1");

        String longPrompt = "  This is a very long prompt that should be trimmed into a shorter conversation title for display purposes.  ";
        when(conversationRepository.save(any(ConversationEntity.class))).thenAnswer(invocation -> {
            ConversationEntity conversation = invocation.getArgument(0);
            conversation.setId("conversation-1");
            return conversation;
        });

        String conversationId = conversationService.ensureConversation(null, project, longPrompt);

        ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(conversationRepository).save(captor.capture());
        assertThat(conversationId).isEqualTo("conversation-1");
        assertThat(captor.getValue().getProject()).isSameAs(project);
        assertThat(captor.getValue().getTitle()).isEqualTo("This is a very long prompt that should be trimmed into...");
    }

    @Test
    void renderConversationHistoryShouldReturnOnlyTheMostRecentMessagesWithinLimit() {
        ConversationMessageEntity first = message(1L, ConversationRole.USER, "Hello");
        ConversationMessageEntity second = message(2L, ConversationRole.ASSISTANT, "Hi there");
        ConversationMessageEntity third = message(3L, ConversationRole.USER, "Tell me more");
        when(conversationMessageRepository.findByConversation_IdOrderByCreatedAtAsc("conversation-1"))
                .thenReturn(List.of(first, second, third));

        String history = conversationService.renderConversationHistory("conversation-1", 2);

        assertThat(history).isEqualTo("ASSISTANT: Hi there\nUSER: Tell me more");
    }

    @Test
    void deleteConversationShouldDeleteMessagesBeforeRemovingConversation() {
        ProjectEntity project = new ProjectEntity();
        project.setId("project-1");
        project.setName("Knowledge Hub");

        ConversationEntity conversation = new ConversationEntity();
        conversation.setId("conversation-1");
        conversation.setProject(project);
        conversation.setTitle("Project chat");
        conversation.setCreatedAt(Instant.parse("2026-04-23T10:00:00Z"));
        conversation.setUpdatedAt(Instant.parse("2026-04-23T10:05:00Z"));

        when(conversationRepository.findByIdAndProject_Id("conversation-1", "project-1"))
                .thenReturn(Optional.of(conversation));
        when(conversationMessageRepository.findByConversation_IdOrderByCreatedAtAsc("conversation-1"))
                .thenReturn(List.of(message(1L, ConversationRole.USER, "Hi")));

        ConversationResponse response = conversationService.getConversation("project-1", "conversation-1");
        conversationService.deleteConversation("project-1", "conversation-1");

        assertThat(response.projectName()).isEqualTo("Knowledge Hub");
        assertThat(response.messages()).hasSize(1);

        InOrder inOrder = inOrder(conversationMessageRepository, conversationRepository);
        inOrder.verify(conversationMessageRepository).deleteByConversation_Id("conversation-1");
        inOrder.verify(conversationRepository).delete(conversation);
    }

    private ConversationMessageEntity message(Long id, ConversationRole role, String content) {
        ConversationMessageEntity message = new ConversationMessageEntity();
        message.setId(id);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(Instant.parse("2026-04-23T10:00:00Z"));
        return message;
    }
}
