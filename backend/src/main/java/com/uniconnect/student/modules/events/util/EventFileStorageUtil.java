package com.uniconnect.student.modules.events.util;

import com.uniconnect.student.common.exception.FileStorageException;
import com.uniconnect.student.common.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Utility class for handling file storage operations.
 * Manages file uploads, downloads, and validation for event evidence files.
 */
@Component
public class EventFileStorageUtil {

    @Value("${file.events-upload-dir}")
    private String uploadDir;

    private Path fileStoragePath;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes
    private static final String ALLOWED_CONTENT_TYPE = "application/pdf";
    private static final String ALLOWED_EXTENSION = ".pdf";

    /**
     * Initialize the file storage directory on application startup.
     */
    @PostConstruct
    public void init() {
        this.fileStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStoragePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create the directory for file uploads", ex);
        }
    }

    /**
     * Store a file in the upload directory with a UUID-based filename.
     *
     * @param file the MultipartFile to store
     * @return the stored file path (relative URL)
     */
    public String storeFile(MultipartFile file) {
        // Validate the file
        validateFile(file);

        // Generate a unique filename using UUID
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            // Resolve the target file path
            Path targetLocation = this.fileStoragePath.resolve(uniqueFilename);

            // Copy the file to the target location (replace if exists)
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return the relative file path as URL
            return "/uploads/events/" + uniqueFilename;

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + uniqueFilename + ". Please try again!", ex);
        }
    }

    /**
     * Load a file as a Resource for download.
     *
     * @param fileName the name of the file to load
     * @return the file as a Resource
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStoragePath.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found: " + fileName, ex);
        }
    }

    /**
     * Validate the uploaded file.
     * Checks file is not empty, is a PDF, and does not exceed 10MB.
     *
     * @param file the MultipartFile to validate
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty. Please upload a valid PDF file.");
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(ALLOWED_EXTENSION)) {
            throw new InvalidFileException("Only PDF files (.pdf) are accepted. Please upload a PDF file.");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase(ALLOWED_CONTENT_TYPE)) {
            throw new InvalidFileException("Invalid file type. Only PDF files are accepted.");
        }

        // Check file size (10MB max)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("File size exceeds the maximum allowed size of 10MB.");
        }
    }

    /**
     * Extract file extension from filename.
     *
     * @param fileName the original filename
     * @return the file extension including the dot
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * Delete a file from the storage directory.
     *
     * @param fileName the name of the file to delete
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStoragePath.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + fileName, ex);
        }
    }

    /**
     * Extract filename from a file URL path.
     *
     * @param fileUrl the file URL/path
     * @return the filename
     */
    public String extractFileName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new FileStorageException("File URL is empty");
        }
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }
}
