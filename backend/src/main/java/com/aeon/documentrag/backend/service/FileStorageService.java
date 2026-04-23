package com.aeon.documentrag.backend.service;

import com.aeon.documentrag.backend.config.StorageProperties;
import com.aeon.documentrag.backend.exception.DocumentIngestionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path uploadDirectory;

    public FileStorageService(StorageProperties storageProperties) {
        this.uploadDirectory = Paths.get(storageProperties.uploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDirectory);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Unable to create upload directory", ex);
        }
    }

    public StoredFile store(MultipartFile multipartFile) {
        try {
            String originalFilename = StringUtils.cleanPath(multipartFile.getOriginalFilename() == null ? "document" : multipartFile.getOriginalFilename());
            String extension = extractExtension(originalFilename);
            String storedFilename = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
            Path destination = uploadDirectory.resolve(storedFilename);
            byte[] fileBytes = multipartFile.getBytes();
            Files.write(destination, fileBytes);
            log.debug("Stored uploaded file {} at {}", originalFilename, destination);
            return new StoredFile(
                    originalFilename,
                    storedFilename,
                    destination,
                    multipartFile.getContentType() == null ? "application/octet-stream" : multipartFile.getContentType(),
                    multipartFile.getSize(),
                    sha256(fileBytes)
            );
        }
        catch (IOException ex) {
            throw new DocumentIngestionException("Unable to store uploaded file", ex);
        }
    }

    public void deleteIfExists(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(storagePath));
        }
        catch (IOException ex) {
            throw new DocumentIngestionException("Unable to delete stored file", ex);
        }
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is not available", ex);
        }
    }

    public record StoredFile(
            String originalFilename,
            String storedFilename,
            Path path,
            String mediaType,
            long sizeBytes,
            String checksum
    ) {
    }
}
