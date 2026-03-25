package com.uniconnect.academic.modules.sessions.controller;

import com.uniconnect.academic.common.dto.ApiResponseDTO;
import com.uniconnect.academic.common.exception.UnauthorizedAccessException;
import com.uniconnect.academic.modules.sessions.dto.SessionRequestDTO;
import com.uniconnect.academic.modules.sessions.dto.SessionResponseDTO;
import com.uniconnect.academic.modules.sessions.service.MentoringSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Mentoring Session operations.
 * Provides endpoints for creating and retrieving mentoring sessions.
 */
@RestController("academicMentoringSessionController")
@RequestMapping("/api/academic/sessions")
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
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole,
            @Valid @RequestBody SessionRequestDTO requestDTO) {

        if (!"MENTOR".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only logged-in academic mentors can create sessions.");
        }

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
            @PathVariable("mentorId") Integer mentorId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole) {

        if (!"MENTOR".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only logged-in academic mentors can view sessions.");
        }

        List<SessionResponseDTO> sessions = mentoringSessionService.getSessionsByMentor(mentorId);

        ApiResponseDTO<List<SessionResponseDTO>> response = ApiResponseDTO.success(
                "Sessions retrieved successfully", sessions);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponseDTO<Void>> cancelSession(
            @PathVariable("sessionId") Integer sessionId,
            @RequestParam("mentorId") Integer mentorId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole) {

        if (!"MENTOR".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only logged-in academic mentors can cancel sessions.");
        }

        mentoringSessionService.cancelSession(sessionId, mentorId);

        ApiResponseDTO<Void> response = ApiResponseDTO.success(
                "Session cancelled successfully", null);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
