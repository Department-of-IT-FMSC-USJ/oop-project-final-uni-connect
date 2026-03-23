package com.uniconnect.student.modules.mentor.controller;

import com.uniconnect.student.common.dto.ApiResponseDTO;
import com.uniconnect.student.modules.mentor.dto.MentorConnectionRequestDTO;
import com.uniconnect.student.modules.mentor.dto.MentorConnectionResponseDTO;
import com.uniconnect.student.modules.mentor.service.MentorConnectionService;
import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Mentor Connection operations.
 * Provides endpoints for requesting and retrieving mentor connections.
 */
@RestController
@RequestMapping("/api/mentor")
public class MentorConnectionController {

    private final MentorConnectionService mentorConnectionService;

    public MentorConnectionController(MentorConnectionService mentorConnectionService) {
        this.mentorConnectionService = mentorConnectionService;
    }

    /**
     * Request a new mentor connection.
     *
     * POST /api/mentor/connect
     *
     * @param requestDTO the mentor connection request details
     * @return the created mentor connection details
     */
    @PostMapping("/connect")
    public ResponseEntity<ApiResponseDTO<MentorConnectionResponseDTO>> requestConnection(
            @Valid @RequestBody MentorConnectionRequestDTO requestDTO) {

        MentorConnectionResponseDTO responseDTO =
                mentorConnectionService.requestConnection(requestDTO);

        ApiResponseDTO<MentorConnectionResponseDTO> response = ApiResponseDTO.success(
                "Mentor connection request submitted successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all mentor connections for a specific student.
     *
     * GET /api/mentor/connections/{studentId}
     *
     * @param studentId the student ID
     * @return list of mentor connections
     */
    @GetMapping("/connections/{studentId}")
    public ResponseEntity<ApiResponseDTO<List<MentorConnectionResponseDTO>>> getConnectionsByStudent(
            @PathVariable("studentId") Integer studentId) {

        List<MentorConnectionResponseDTO> connections =
                mentorConnectionService.getConnectionsByStudent(studentId);

        ApiResponseDTO<List<MentorConnectionResponseDTO>> response = ApiResponseDTO.success(
                "Mentor connections retrieved successfully", connections);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<ApiResponseDTO<List<MentorConnectionResponseDTO>>> getConnectionsByMentor(
            @PathVariable("mentorId") Integer mentorId) {

        List<MentorConnectionResponseDTO> connections = mentorConnectionService.getConnectionsByMentor(mentorId);

        ApiResponseDTO<List<MentorConnectionResponseDTO>> response = ApiResponseDTO.success(
                "Mentor connections retrieved successfully", connections);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/industry/auto-assign/{studentId}")
    public ResponseEntity<ApiResponseDTO<MentorConnectionResponseDTO>> autoAssignIndustryMentor(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("studentId") Integer studentId) {

        if (currentUser.getRole() == Role.UNDERGRADUATE
                && !currentUser.getId().equals(studentId.longValue())) {
            return new ResponseEntity<>(ApiResponseDTO.error("You can only trigger mentor assignment for your account."),
                    HttpStatus.FORBIDDEN);
        }

        MentorConnectionResponseDTO responseDTO = mentorConnectionService.autoAssignIndustryMentor(studentId);
        ApiResponseDTO<MentorConnectionResponseDTO> response = ApiResponseDTO.success(
                "Industry mentor assigned successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
