package com.uniconnect.service;

import com.uniconnect.dto.StudyMaterialRequestDTO;
import com.uniconnect.dto.StudyMaterialResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for Study Material operations.
 */
public interface StudyMaterialService {

    /**
     * Upload a new study material.
     *
     * @param requestDTO the study material details
     * @param file       the PDF file to upload
     * @param studentId  the ID of the student uploading
     * @param userRole   the role of the user
     * @return the uploaded study material details
     */
    StudyMaterialResponseDTO uploadMaterial(StudyMaterialRequestDTO requestDTO,
                                            MultipartFile file,
                                            Integer studentId,
                                            String userRole);

    /**
     * Get all study materials.
     *
     * @return list of all study materials
     */
    List<StudyMaterialResponseDTO> getAllMaterials();

    /**
     * Download a study material file by ID.
     *
     * @param materialId the ID of the material to download
     * @return the file as a Resource
     */
    Resource downloadMaterial(Integer materialId);

    /**
     * Get the filename for a material by ID.
     *
     * @param materialId the ID of the material
     * @return the filename
     */
    String getMaterialFileName(Integer materialId);
}
