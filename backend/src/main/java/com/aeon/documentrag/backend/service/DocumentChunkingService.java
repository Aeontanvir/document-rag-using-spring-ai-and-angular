package com.aeon.documentrag.backend.service;

import com.aeon.documentrag.backend.config.RagProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DocumentChunkingService {

    private final RagProperties ragProperties;

    public List<Document> chunk(String documentId,
                                String projectId,
                                String projectName,
                                String originalFilename,
                                String mediaType,
                                String checksum,
                                List<Document> extractedDocuments) {
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(ragProperties.chunkSize())
                .withMinChunkSizeChars(ragProperties.minChunkSizeChars())
                .withMinChunkLengthToEmbed(ragProperties.minChunkLengthToEmbed())
                .withMaxNumChunks(ragProperties.maxNumChunks())
                .withKeepSeparator(ragProperties.keepSeparator())
                .build();

        List<Document> enrichedDocuments = extractedDocuments.stream()
                .filter(Document::isText)
                .filter(document -> document.getText() != null && !document.getText().isBlank())
                .map(document -> new Document(document.getText(), mergeMetadata(
                        document.getMetadata(),
                        documentId,
                        projectId,
                        projectName,
                        originalFilename,
                        mediaType,
                        checksum
                )))
                .toList();

        List<Document> chunks = splitter.apply(enrichedDocuments);

        return IntStream.range(0, chunks.size())
                .mapToObj(index -> {
                    Document chunk = chunks.get(index);
                    Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
                    metadata.put("chunkIndex", index + 1);
                    metadata.put("sourceFileName", originalFilename);
                    return new Document(buildChunkId(documentId, index + 1), chunk.getText(), metadata);
                })
                .toList();
    }

    public String buildChunkId(String documentId, int chunkIndex) {
        return documentId + "-chunk-" + chunkIndex;
    }

    private Map<String, Object> mergeMetadata(Map<String, Object> existingMetadata,
                                              String documentId,
                                              String projectId,
                                              String projectName,
                                              String originalFilename,
                                              String mediaType,
                                              String checksum) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.putAll(existingMetadata);
        metadata.put("documentId", documentId);
        metadata.put("projectId", projectId);
        metadata.put("projectName", projectName);
        metadata.put("sourceFileName", originalFilename);
        metadata.put("mediaType", mediaType);
        metadata.put("checksum", checksum);
        return metadata;
    }
}
