package com.aeon.documentrag.backend.service;

import com.aeon.documentrag.backend.config.RagProperties;
import com.aeon.documentrag.backend.dto.ChatRequest;
import com.aeon.documentrag.backend.dto.ChatResponse;
import com.aeon.documentrag.backend.dto.DocumentCitationResponse;
import com.aeon.documentrag.backend.entity.type.ConversationRole;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class RagChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final RagProperties ragProperties;
    private final ConversationService conversationService;

    public RagChatService(ChatClient.Builder chatClientBuilder,
                          VectorStore vectorStore,
                          RagProperties ragProperties,
                          ConversationService conversationService) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.ragProperties = ragProperties;
        this.conversationService = conversationService;
    }

    public ChatResponse chat(ChatRequest request) {
        String conversationId = conversationService.ensureConversation(request.conversationId(), request.prompt());
        String history = conversationService.renderConversationHistory(conversationId, ragProperties.conversationHistoryLimit());
        SearchRequest searchRequest = SearchRequest.builder()
                .query(request.prompt())
                .topK(request.topK() == null ? ragProperties.defaultTopK() : request.topK())
                .similarityThreshold(ragProperties.similarityThreshold())
                .build();

        List<Document> retrievedDocuments = vectorStore.similaritySearch(searchRequest);
        String response = chatClient.prompt()
                .system(ragProperties.systemPrompt())
                .user(buildUserPrompt(history, request.prompt()))
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(searchRequest)
                        .build())
                .call()
                .content();

        conversationService.appendMessage(conversationId, ConversationRole.USER, request.prompt());
        conversationService.appendMessage(conversationId, ConversationRole.ASSISTANT, response);

        return new ChatResponse(
                conversationId,
                response,
                retrievedDocuments.stream().map(this::toCitation).toList(),
                Instant.now()
        );
    }

    private String buildUserPrompt(String history, String question) {
        String normalizedHistory = history == null || history.isBlank() ? "No prior conversation." : history;
        return """
                Conversation history:
                %s

                Current user question:
                %s
                """.formatted(normalizedHistory, question);
    }

    private DocumentCitationResponse toCitation(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        return new DocumentCitationResponse(
                document.getId(),
                metadata.getOrDefault("documentId", "").toString(),
                metadata.getOrDefault("sourceFileName", "Unknown").toString(),
                metadata.containsKey("chunkIndex") ? Integer.parseInt(metadata.get("chunkIndex").toString()) : null,
                abbreviate(document.getText())
        );
    }

    private String abbreviate(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String normalized = text.strip();
        return normalized.length() > 220 ? normalized.substring(0, 217) + "..." : normalized;
    }
}
