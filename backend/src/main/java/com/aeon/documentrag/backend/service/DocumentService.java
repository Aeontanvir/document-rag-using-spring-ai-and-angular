package com.aeon.documentrag.backend.service;

import com.aeon.documentrag.backend.dto.DocumentMetadataResponse;
import com.aeon.documentrag.backend.dto.UploadBatchResponse;
import com.aeon.documentrag.backend.entity.DocumentRecordEntity;
import com.aeon.documentrag.backend.entity.type.DocumentStatus;
import com.aeon.documentrag.backend.exception.DocumentIngestionException;
import com.aeon.documentrag.backend.exception.ResourceNotFoundException;
import com.aeon.documentrag.backend.exception.UnsupportedDocumentTypeException;
import com.aeon.documentrag.backend.mapper.DocumentMapper;
import com.aeon.documentrag.backend.repository.DocumentRecordRepository;
import jakarta.transaction.Transactional;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.IntStream;

@Service
public class DocumentService {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "txt", "md", "html", "htm", "rtf", "ppt", "pptx"
    );

    private final DocumentRecordRepository documentRecordRepository;
    private final FileStorageService fileStorageService;
    private final DocumentChunkingService documentChunkingService;
    private final VectorStore vectorStore;

    public DocumentService(DocumentRecordRepository documentRecordRepository,
                           FileStorageService fileStorageService,
                           DocumentChunkingService documentChunkingService,
                           VectorStore vectorStore) {
        this.documentRecordRepository = documentRecordRepository;
        this.fileStorageService = fileStorageService;
        this.documentChunkingService = documentChunkingService;
        this.vectorStore = vectorStore;
    }

    @Transactional
    public UploadBatchResponse ingest(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one file must be uploaded");
        }

        List<DocumentMetadataResponse> uploadedDocuments = files.stream()
                .map(this::ingestSingle)
                .toList();

        return new UploadBatchResponse(uploadedDocuments.size(), uploadedDocuments);
    }

    public List<DocumentMetadataResponse> listDocuments() {
        return documentRecordRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(DocumentMapper::toResponse)
                .toList();
    }

    public DocumentMetadataResponse getDocument(String documentId) {
        return DocumentMapper.toResponse(findDocument(documentId));
    }

    @Transactional
    public void deleteDocument(String documentId) {
        DocumentRecordEntity entity = findDocument(documentId);
        List<String> chunkIds = IntStream.rangeClosed(1, entity.getChunkCount())
                .mapToObj(index -> documentChunkingService.buildChunkId(documentId, index))
                .toList();
        if (!chunkIds.isEmpty()) {
            vectorStore.delete(chunkIds);
        }
        fileStorageService.deleteIfExists(entity.getStoragePath());
        documentRecordRepository.delete(entity);
    }

    private DocumentMetadataResponse ingestSingle(MultipartFile file) {
        validateFile(file);
        FileStorageService.StoredFile storedFile = fileStorageService.store(file);

        DocumentRecordEntity record = new DocumentRecordEntity();
        record.setOriginalFilename(storedFile.originalFilename());
        record.setStoredFilename(storedFile.storedFilename());
        record.setStoragePath(storedFile.path().toString());
        record.setMediaType(storedFile.mediaType());
        record.setSizeBytes(storedFile.sizeBytes());
        record.setChecksum(storedFile.checksum());
        record.setChunkCount(0);
        record.setStatus(DocumentStatus.INDEXING);
        DocumentRecordEntity savedRecord = documentRecordRepository.save(record);

        try {
            List<Document> extractedDocuments = new TikaDocumentReader(new FileSystemResource(storedFile.path())).read();
            List<Document> chunks = documentChunkingService.chunk(
                    savedRecord.getId(),
                    savedRecord.getOriginalFilename(),
                    savedRecord.getMediaType(),
                    savedRecord.getChecksum(),
                    extractedDocuments
            );

            if (chunks.isEmpty()) {
                throw new DocumentIngestionException("No readable content could be extracted from " + savedRecord.getOriginalFilename());
            }

            vectorStore.add(chunks);
            savedRecord.setChunkCount(chunks.size());
            savedRecord.setStatus(DocumentStatus.INDEXED);
            savedRecord.setFailureReason(null);
            return DocumentMapper.toResponse(documentRecordRepository.save(savedRecord));
        }
        catch (RuntimeException ex) {
            savedRecord.setStatus(DocumentStatus.FAILED);
            savedRecord.setFailureReason(ex.getMessage());
            documentRecordRepository.save(savedRecord);
            throw ex;
        }
    }

    private DocumentRecordEntity findDocument(String documentId) {
        return documentRecordRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new UnsupportedDocumentTypeException("Uploaded file must include a supported extension");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new UnsupportedDocumentTypeException("Unsupported file type: ." + extension);
        }
    }
}
