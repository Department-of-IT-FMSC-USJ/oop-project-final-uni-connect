package com.uniconnect.student.modules.materials.controller;

import com.uniconnect.student.common.dto.ApiResponseDTO;
import com.uniconnect.student.modules.materials.dto.StudyMaterialRequestDTO;
import com.uniconnect.student.modules.materials.dto.StudyMaterialResponseDTO;
import com.uniconnect.student.modules.materials.service.StudyMaterialService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for Study Material operations.
 * Provides endpoints for uploading, listing, and downloading study materials.
 */
@RestController
@RequestMapping("/api/materials")
public class StudyMaterialController {

    private final StudyMaterialService studyMaterialService;

    public StudyMaterialController(StudyMaterialService studyMaterialService) {
        this.studyMaterialService = studyMaterialService;
    }

    /**
     * Upload a new study material.
     *
     * POST /api/materials/upload
     *
     * @param title        the title of the material
     * @param description  optional description
     * @param materialType the type of material (Notes, Past Papers, Lecture Slides, Other)
     * @param file         the PDF file to upload
     * @param studentId    the ID of the uploading student (passed via header)
     * @param userRole     the role of the user (passed via header)
     * @return the uploaded material details
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponseDTO<StudyMaterialResponseDTO>> uploadMaterial(
            @Valid @RequestPart("material") StudyMaterialRequestDTO requestDTO,
            @RequestPart("file") MultipartFile file,
            @RequestHeader("X-User-Id") Integer studentId,
            @RequestHeader("X-User-Role") String userRole) {

        StudyMaterialResponseDTO responseDTO = studyMaterialService.uploadMaterial(
                requestDTO, file, studentId, userRole);

        ApiResponseDTO<StudyMaterialResponseDTO> response = ApiResponseDTO.success(
                "Study material uploaded successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all study materials.
     *
     * GET /api/materials
     *
     * @return list of all study materials
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<StudyMaterialResponseDTO>>> getAllMaterials() {

        List<StudyMaterialResponseDTO> materials = studyMaterialService.getAllMaterials();

        ApiResponseDTO<List<StudyMaterialResponseDTO>> response = ApiResponseDTO.success(
                "Study materials retrieved successfully", materials);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Download a study material file by ID.
     *
     * GET /api/materials/download/{id}
     *
     * @param id       the material ID
     * @param userId   the authenticated user's ID (passed via header)
     * @param userRole the authenticated user's role (passed via header)
     * @return the PDF file as a downloadable response
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadMaterial(
            @PathVariable("id") Integer id,
            @RequestHeader("X-User-Id") Integer userId,
            @RequestHeader("X-User-Role") String userRole) {

        // Verify authentication by checking if headers are present
        if (userId == null || userRole == null || userRole.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Resource resource = studyMaterialService.downloadMaterial(id);
        String fileName = studyMaterialService.getMaterialFileName(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    /**
     * Delete a study material by ID.
     * Only the user who uploaded the material can delete it.
     *
     * DELETE /api/materials/{id}
     *
     * @param id       the material ID
     * @param userId   the authenticated user's ID (passed via header)
     * @param userRole the authenticated user's role (passed via header)
     * @return success or error response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteMaterial(
            @PathVariable("id") Integer id,
            @RequestHeader("X-User-Id") Integer userId,
            @RequestHeader("X-User-Role") String userRole) {

        // Verify authentication
        if (userId == null || userRole == null || userRole.isBlank()) {
            return new ResponseEntity<>(
                    ApiResponseDTO.error("Authentication required"),
                    HttpStatus.UNAUTHORIZED);
        }

        studyMaterialService.deleteMaterial(id, userId, userRole);

        ApiResponseDTO<Void> response = ApiResponseDTO.success(
                "Study material deleted successfully", null);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
