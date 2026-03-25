package com.uniconnect.academic.modules.contactinfo.controller;

import com.uniconnect.academic.common.dto.ApiResponseDTO;
import com.uniconnect.academic.modules.contactinfo.dto.ContactInfoRequestDTO;
import com.uniconnect.academic.modules.contactinfo.dto.ContactInfoResponseDTO;
import com.uniconnect.academic.modules.contactinfo.service.MentorContactInfoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Mentor Contact Information operations.
 * Provides endpoints for managing and viewing mentor contact details.
 */
@RestController
@RequestMapping("/api/contact-info")
public class MentorContactInfoController {

    private final MentorContactInfoService mentorContactInfoService;

    public MentorContactInfoController(MentorContactInfoService mentorContactInfoService) {
        this.mentorContactInfoService = mentorContactInfoService;
    }

    /**
     * Save or update mentor contact information.
     *
     * POST /api/contact-info
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ContactInfoResponseDTO>> saveContactInfo(
            @Valid @RequestBody ContactInfoRequestDTO requestDTO) {

        ContactInfoResponseDTO responseDTO = mentorContactInfoService.saveContactInfo(requestDTO);

        ApiResponseDTO<ContactInfoResponseDTO> response = ApiResponseDTO.success(
                "Contact information saved successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get mentor contact information.
     *
     * GET /api/contact-info/mentor/{mentorId}
     */
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<ApiResponseDTO<ContactInfoResponseDTO>> getContactInfo(
            @PathVariable("mentorId") Integer mentorId) {

        ContactInfoResponseDTO responseDTO = mentorContactInfoService.getContactInfoByMentor(mentorId);

        ApiResponseDTO<ContactInfoResponseDTO> response = ApiResponseDTO.success(
                "Contact information retrieved successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
