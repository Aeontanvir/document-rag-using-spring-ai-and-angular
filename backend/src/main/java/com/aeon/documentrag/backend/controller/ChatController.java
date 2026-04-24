package com.aeon.documentrag.backend.controller;

import com.aeon.documentrag.backend.dto.ChatRequest;
import com.aeon.documentrag.backend.dto.ChatResponse;
import com.aeon.documentrag.backend.dto.ConversationResponse;
import com.aeon.documentrag.backend.security.AuthenticatedUser;
import com.aeon.documentrag.backend.service.ConversationService;
import com.aeon.documentrag.backend.service.ProjectService;
import com.aeon.documentrag.backend.service.RagChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@RestController
@RequestMapping("/api/v1/projects/{projectId}/chat")
@Tag(name = "Chat", description = "Project-scoped conversational RAG endpoints")
@RequiredArgsConstructor
public class ChatController {

    private final RagChatService ragChatService;
    private final ConversationService conversationService;
    private final ProjectService projectService;

    @PostMapping("/messages")
    @Operation(
            summary = "Submit a chat message",
            description = "Runs retrieval against ChromaDB and answers with Spring AI using the configured Ollama model.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Chat response returned"),
                    @ApiResponse(responseCode = "400", description = "Invalid prompt", content = @Content(schema = @Schema(hidden = true)))
            }
    )
    public ResponseEntity<ChatResponse> sendMessage(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                    @PathVariable String projectId,
                                                    @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(ragChatService.chat(authenticatedUser.id(), projectId, request));
    }

    @GetMapping("/conversations/{conversationId}")
    @Operation(summary = "Get conversation history")
    public ResponseEntity<ConversationResponse> getConversation(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                                @PathVariable String projectId,
                                                                @PathVariable String conversationId) {
        projectService.getProjectEntity(authenticatedUser.id(), projectId);
        return ResponseEntity.ok(conversationService.getConversation(projectId, conversationId));
    }

    @DeleteMapping("/conversations/{conversationId}")
    @Operation(summary = "Delete a conversation")
    public ResponseEntity<Void> deleteConversation(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                   @PathVariable String projectId,
                                                   @PathVariable String conversationId) {
        projectService.getProjectEntity(authenticatedUser.id(), projectId);
        conversationService.deleteConversation(projectId, conversationId);
        return ResponseEntity.noContent().build();
    }
}
