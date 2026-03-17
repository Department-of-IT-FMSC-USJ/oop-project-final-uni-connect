package com.hodportal.modules.mentorverification.controller;

import com.hodportal.common.dto.ApiResponseDTO;
import com.hodportal.common.exception.UnauthorizedAccessException;
import com.hodportal.modules.mentorverification.dto.VerificationRequestDTO;
import com.hodportal.modules.mentorverification.dto.VerificationResponseDTO;
import com.hodportal.modules.mentorverification.service.MentorVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Industry Mentor Verification operations.
 * Only HoD users can access these endpoints.
 */
@RestController
@RequestMapping("/api/mentor-verifications")
public class MentorVerificationController {

    private final MentorVerificationService mentorVerificationService;

    public MentorVerificationController(MentorVerificationService mentorVerificationService) {
        this.mentorVerificationService = mentorVerificationService;
    }

    /**
     * Get all industry mentor verification records.
     *
     * GET /api/mentor-verifications
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<VerificationResponseDTO>>> getAllMentorVerifications(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole) {

        if (!"HOD".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only HoD users can access mentor verification records.");
        }

        List<VerificationResponseDTO> verifications = mentorVerificationService.getAllMentorVerifications();

        ApiResponseDTO<List<VerificationResponseDTO>> response = ApiResponseDTO.success(
                "Mentor verifications retrieved successfully", verifications);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update the verification status of an industry mentor.
     *
     * PUT /api/mentor-verifications/{mentorId}/verify
     */
    @PutMapping("/{mentorId}/verify")
    public ResponseEntity<ApiResponseDTO<VerificationResponseDTO>> updateVerificationStatus(
            @PathVariable("mentorId") Integer mentorId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") Integer hodId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole,
            @Valid @RequestBody VerificationRequestDTO requestDTO) {

        if (!"HOD".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only HoD users can verify industry mentor profiles.");
        }

        VerificationResponseDTO responseDTO = mentorVerificationService.updateVerificationStatus(
                mentorId, hodId, requestDTO);

        ApiResponseDTO<VerificationResponseDTO> response = ApiResponseDTO.success(
                "Mentor verification status updated successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
