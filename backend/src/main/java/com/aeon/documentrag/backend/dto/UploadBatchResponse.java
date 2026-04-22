package com.aeon.documentrag.backend.dto;

import java.util.List;

public record UploadBatchResponse(
        int uploadedCount,
        List<DocumentMetadataResponse> documents
) {
}
