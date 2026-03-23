package com.uniconnect.industry.modules.sessions.controller;

import com.uniconnect.industry.common.dto.ApiResponseDTO;
import com.uniconnect.industry.common.exception.UnauthorizedAccessException;
import com.uniconnect.industry.modules.sessions.dto.SessionRequestDTO;
import com.uniconnect.industry.modules.sessions.dto.SessionResponseDTO;
import com.uniconnect.industry.modules.sessions.service.MentoringSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("industryMentoringSessionController")
@RequestMapping("/api/industry/sessions")
public class MentoringSessionController {

    private final MentoringSessionService mentoringSessionService;

    public MentoringSessionController(MentoringSessionService mentoringSessionService) {
        this.mentoringSessionService = mentoringSessionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<SessionResponseDTO>> createSession(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole,
            @Valid @RequestBody SessionRequestDTO requestDTO) {

        if (!"MENTOR".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only logged-in industry mentors can create sessions.");
        }

        SessionResponseDTO responseDTO = mentoringSessionService.createSession(requestDTO);
        ApiResponseDTO<SessionResponseDTO> response = ApiResponseDTO.success(
                "Mentoring session created successfully", responseDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<ApiResponseDTO<List<SessionResponseDTO>>> getSessionsByMentor(
            @PathVariable("mentorId") Integer mentorId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole) {

        if (!"MENTOR".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only logged-in industry mentors can view sessions.");
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
            throw new UnauthorizedAccessException("Only logged-in industry mentors can cancel sessions.");
        }

        mentoringSessionService.cancelSession(sessionId, mentorId);
        ApiResponseDTO<Void> response = ApiResponseDTO.success("Session cancelled successfully", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
