package com.academicmentor.modules.sessions.controller;

import com.academicmentor.common.dto.ApiResponseDTO;
import com.academicmentor.modules.sessions.dto.SessionRequestDTO;
import com.academicmentor.modules.sessions.dto.SessionResponseDTO;
import com.academicmentor.modules.sessions.service.MentoringSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Mentoring Session operations.
 * Provides endpoints for creating and retrieving mentoring sessions.
 */
@RestController
@RequestMapping("/api/sessions")
public class MentoringSessionController {

    private final MentoringSessionService mentoringSessionService;

    public MentoringSessionController(MentoringSessionService mentoringSessionService) {
        this.mentoringSessionService = mentoringSessionService;
    }

    /**
     * Create a new mentoring session.
     *
     * POST /api/sessions
     *
     * @param requestDTO the session details
     * @return the created session details
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<SessionResponseDTO>> createSession(
            @Valid @RequestBody SessionRequestDTO requestDTO) {

        SessionResponseDTO responseDTO = mentoringSessionService.createSession(requestDTO);

        ApiResponseDTO<SessionResponseDTO> response = ApiResponseDTO.success(
                "Mentoring session created successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all sessions for a specific mentor.
     *
     * GET /api/sessions/mentor/{mentorId}
     *
     * @param mentorId the mentor ID
     * @return list of session records
     */
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<ApiResponseDTO<List<SessionResponseDTO>>> getSessionsByMentor(
            @PathVariable("mentorId") Integer mentorId) {

        List<SessionResponseDTO> sessions = mentoringSessionService.getSessionsByMentor(mentorId);

        ApiResponseDTO<List<SessionResponseDTO>> response = ApiResponseDTO.success(
                "Sessions retrieved successfully", sessions);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
