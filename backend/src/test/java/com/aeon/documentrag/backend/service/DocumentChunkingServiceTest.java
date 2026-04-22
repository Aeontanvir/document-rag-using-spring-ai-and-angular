package com.aeon.documentrag.backend.service;

import com.aeon.documentrag.backend.config.RagProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentChunkingServiceTest {

    @Test
    void shouldKeepDocumentMetadataWhenChunking() {
        DocumentChunkingService service = new DocumentChunkingService(new RagProperties(
                40,
                20,
                5,
                20,
                true,
                0.5,
                5,
                8,
                "prompt"
        ));

        List<Document> chunks = service.chunk(
                "doc-123",
                "sample.txt",
                "text/plain",
                "abc123",
                List.of(new Document("Spring AI with ChromaDB creates grounded answers. ".repeat(10), Map.of("source", "unit-test")))
        );

        assertThat(chunks).isNotEmpty();
        assertThat(chunks.getFirst().getMetadata())
                .containsEntry("documentId", "doc-123")
                .containsEntry("sourceFileName", "sample.txt")
                .containsEntry("checksum", "abc123")
                .containsKey("chunkIndex");
        assertThat(chunks.getFirst().getId()).startsWith("doc-123-chunk-");
    }
}
