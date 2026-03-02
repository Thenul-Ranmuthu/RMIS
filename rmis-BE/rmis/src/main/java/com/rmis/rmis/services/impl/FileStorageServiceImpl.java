package com.rmis.rmis.services.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.rmis.rmis.exceptions.FileStorageException;


@Service
public class FileStorageServiceImpl {
    @Value("${app.file.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "application/pdf",
        "image/jpeg",
        "image/png"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    // Store a certification file under uploads/certifications/{technicianId}/
    public String storeCertification(MultipartFile file, Long technicianId) {

        // 1. Validate
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot upload empty file");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new FileStorageException("Only PDF, JPG, PNG files are allowed");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException("File size must be less than 5MB");
        }

        try {
            // 2. Create directory for this technician
            // Path: uploads/certifications/{technicianId}/
            Path technicianDir = Paths.get(uploadDir, String.valueOf(technicianId));
            System.out.println("Saving file to: " + technicianDir.toAbsolutePath());
            if (!Files.exists(technicianDir)) {
                Files.createDirectories(technicianDir);
            }

            // 3. Generate unique file name
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            // 4. Save file to disk
            Path targetPath = technicianDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 5. Return relative path to store in DB
            return targetPath.toString();

        } catch (IOException e) {
            throw new FileStorageException("Could not store file: " + e.getMessage());
        }
    }

    // Load a file for viewing/downloading
    public Resource loadFile(String filePath) {
        try {
            Path path = Paths.get(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File not found: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("File not found: " + filePath);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            throw new FileStorageException("Could not delete file: " + e.getMessage());
        }
    }
}
