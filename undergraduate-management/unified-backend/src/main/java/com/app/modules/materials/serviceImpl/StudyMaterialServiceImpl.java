package com.app.modules.materials.serviceImpl;

import com.app.modules.materials.dto.StudyMaterialRequestDTO;
import com.app.modules.materials.dto.StudyMaterialResponseDTO;
import com.app.modules.materials.entity.StudyMaterial;
import com.app.modules.materials.enums.UserRole;
import com.app.common.exception.ResourceNotFoundException;
import com.app.common.exception.UnauthorizedAccessException;
import com.app.modules.materials.repository.StudyMaterialRepository;
import com.app.modules.materials.service.StudyMaterialService;
import com.app.modules.materials.util.FileStorageUtil;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of StudyMaterialService.
 * Handles business logic for study material operations.
 */
@Service
public class StudyMaterialServiceImpl implements StudyMaterialService {

    private final StudyMaterialRepository studyMaterialRepository;
    private final FileStorageUtil fileStorageUtil;

    public StudyMaterialServiceImpl(StudyMaterialRepository studyMaterialRepository,
                                     FileStorageUtil fileStorageUtil) {
        this.studyMaterialRepository = studyMaterialRepository;
        this.fileStorageUtil = fileStorageUtil;
    }

    /**
     * Upload a new study material.
     * Only users with role UNDERGRADUATE are allowed to upload.
     */
    @Override
    public StudyMaterialResponseDTO uploadMaterial(StudyMaterialRequestDTO requestDTO,
                                                    MultipartFile file,
                                                    Integer studentId,
                                                    String userRole) {

        // Validate user role - only UNDERGRADUATE can upload
        if (!UserRole.UNDERGRADUATE.name().equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException(
                    "Only users with role 'UNDERGRADUATE' are allowed to upload study materials.");
        }

        // Store the file and get the file URL
        String fileUrl = fileStorageUtil.storeFile(file);

        // Create and save the entity
        StudyMaterial studyMaterial = new StudyMaterial();
        studyMaterial.setTitle(requestDTO.getTitle());
        studyMaterial.setDescription(requestDTO.getDescription());
        studyMaterial.setMaterialType(requestDTO.getMaterialType());
        studyMaterial.setFileUrl(fileUrl);
        studyMaterial.setUploadedBy(studentId);

        StudyMaterial savedMaterial = studyMaterialRepository.save(studyMaterial);

        return mapToResponseDTO(savedMaterial);
    }

    /**
     * Get all study materials ordered by upload date (newest first).
     */
    @Override
    public List<StudyMaterialResponseDTO> getAllMaterials() {
        List<StudyMaterial> materials = studyMaterialRepository.findAllByOrderByUploadDateDesc();

        return materials.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Download a study material file by ID.
     */
    @Override
    public Resource downloadMaterial(Integer materialId) {
        StudyMaterial material = studyMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Study material not found with ID: " + materialId));

        String fileName = fileStorageUtil.extractFileName(material.getFileUrl());
        return fileStorageUtil.loadFileAsResource(fileName);
    }

    /**
     * Get the filename for a material by ID.
     */
    @Override
    public String getMaterialFileName(Integer materialId) {
        StudyMaterial material = studyMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Study material not found with ID: " + materialId));

        return fileStorageUtil.extractFileName(material.getFileUrl());
    }

    /**
     * Delete a study material by ID.
     * Only the user who uploaded the material can delete it.
     */
    @Override
    public void deleteMaterial(Integer materialId, Integer userId, String userRole) {
        StudyMaterial material = studyMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Study material not found with ID: " + materialId));

        // Only the uploader can delete their own material
        if (!material.getUploadedBy().equals(userId)) {
            throw new UnauthorizedAccessException(
                    "You can only delete study materials that you uploaded.");
        }

        // Delete the physical file
        String fileName = fileStorageUtil.extractFileName(material.getFileUrl());
        fileStorageUtil.deleteFile(fileName);

        // Delete the database record
        studyMaterialRepository.delete(material);
    }

    /**
     * Map StudyMaterial entity to StudyMaterialResponseDTO.
     *
     * @param material the entity to map
     * @return the response DTO
     */
    private StudyMaterialResponseDTO mapToResponseDTO(StudyMaterial material) {
        return StudyMaterialResponseDTO.builder()
                .materialId(material.getMaterialId())
                .title(material.getTitle())
                .description(material.getDescription())
                .materialType(material.getMaterialType())
                .fileUrl(material.getFileUrl())
                .uploadedBy(material.getUploadedBy())
                .uploadDate(material.getUploadDate())
                .build();
    }
}
