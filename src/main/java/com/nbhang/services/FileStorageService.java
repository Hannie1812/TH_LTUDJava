package com.nbhang.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:src/main/resources/static/images/books}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Store uploaded file and return the filename
     */
    public String storeFile(MultipartFile file, Long bookId) throws IOException {
        return storeFileGeneric(file, "book", bookId);
    }

    public String storeInvoiceProof(MultipartFile file, Long invoiceId) throws IOException {
        return storeFileGeneric(file, "invoice_proof", invoiceId);
    }

    private String storeFileGeneric(MultipartFile file, String prefix, Long id) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum limit of 5MB");
        }

        // Validate file type
        String originalFilename = file.getOriginalFilename();
        if (!isValidImageFile(originalFilename)) {
            throw new IOException("Invalid file type. Only JPG, JPEG, PNG, and GIF are allowed");
        }

        // Generate unique filename
        String extension = getFileExtension(originalFilename);
        long timestamp = System.currentTimeMillis();
        String filename = String.format("%s_%d_%d.%s", prefix, id, timestamp, extension);

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    /**
     * Delete file from storage
     */
    public void deleteFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }

        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Failed to delete file: " + filename);
        }
    }

    /**
     * Validate if file is a valid image
     */
    private boolean isValidImageFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        String extension = getFileExtension(filename);
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
}
