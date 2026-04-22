package com.aeon.documentrag.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rag")
public record RagProperties(
        int chunkSize,
        int minChunkSizeChars,
        int minChunkLengthToEmbed,
        int maxNumChunks,
        boolean keepSeparator,
        double similarityThreshold,
        int defaultTopK,
        int conversationHistoryLimit,
        String systemPrompt
) {
}
