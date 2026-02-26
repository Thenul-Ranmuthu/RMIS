package com.rmis.rmis.services.impl;

import com.rmis.rmis.config.FileStorageProperties;
import com.rmis.rmis.exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final List<String> allowedTypes;
    private final long maxFileSize;

    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        this.allowedTypes = Arrays.asList(fileStorageProperties.getAllowedTypes().split(","));
        this.maxFileSize = fileStorageProperties.getMaxFileSize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File upload directory created: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create upload directory", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Validate file is not empty
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file");
        }

        // Get file extension
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);

        // Validate file type
        if (fileExtension == null || !allowedTypes.contains(fileExtension.toLowerCase())) {
            throw new FileStorageException("Invalid file type. Allowed types: " + String.join(", ", allowedTypes));
        }

        // Validate file size
        if (file.getSize() > maxFileSize) {
            throw new FileStorageException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored successfully: {}", fileName);
            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("File deleted successfully: {}", fileName);
            }
        } catch (IOException ex) {
            log.error("Could not delete file: {}", fileName, ex);
            throw new FileStorageException("Could not delete file " + fileName, ex);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}