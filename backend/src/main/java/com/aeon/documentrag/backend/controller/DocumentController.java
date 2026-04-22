package com.aeon.documentrag.backend.controller;

import com.aeon.documentrag.backend.dto.DocumentMetadataResponse;
import com.aeon.documentrag.backend.dto.UploadBatchResponse;
import com.aeon.documentrag.backend.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Document ingestion and catalog endpoints")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Ingest documents",
            description = "Uploads supported documents, extracts text with Apache Tika, chunks content, and stores embeddings in ChromaDB.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Documents ingested"),
                    @ApiResponse(responseCode = "400", description = "Unsupported or invalid file", content = @Content(schema = @Schema(hidden = true)))
            }
    )
    public ResponseEntity<UploadBatchResponse> ingestDocuments(@RequestPart("files") List<MultipartFile> files) {
        return ResponseEntity.ok(documentService.ingest(files));
    }

    @GetMapping
    @Operation(summary = "List ingested documents", responses = {
            @ApiResponse(responseCode = "200", description = "Document list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DocumentMetadataResponse.class))))
    })
    public ResponseEntity<List<DocumentMetadataResponse>> listDocuments() {
        return ResponseEntity.ok(documentService.listDocuments());
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Get a document record")
    public ResponseEntity<DocumentMetadataResponse> getDocument(@PathVariable String documentId) {
        return ResponseEntity.ok(documentService.getDocument(documentId));
    }

    @DeleteMapping("/{documentId}")
    @Operation(summary = "Delete a document and its vector chunks")
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}
