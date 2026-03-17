package com.app.modules.feedback.controller;

import com.app.common.dto.ApiResponseDTO;
import com.app.modules.feedback.dto.MentorFeedbackRequestDTO;
import com.app.modules.feedback.dto.MentorFeedbackResponseDTO;
import com.app.modules.feedback.service.MentorFeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Mentor Feedback operations.
 * Provides endpoints for submitting and retrieving mentor feedback.
 */
@RestController
@RequestMapping("/api/feedback")
public class MentorFeedbackController {

    private final MentorFeedbackService mentorFeedbackService;

    public MentorFeedbackController(MentorFeedbackService mentorFeedbackService) {
        this.mentorFeedbackService = mentorFeedbackService;
    }

    /**
     * Submit feedback for a completed mentor session.
     *
     * POST /api/feedback
     *
     * @param requestDTO the feedback details
     * @return the submitted feedback details
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<MentorFeedbackResponseDTO>> submitFeedback(
            @Valid @RequestBody MentorFeedbackRequestDTO requestDTO) {

        MentorFeedbackResponseDTO responseDTO =
                mentorFeedbackService.submitFeedback(requestDTO);

        ApiResponseDTO<MentorFeedbackResponseDTO> response = ApiResponseDTO.success(
                "Feedback submitted successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all feedback for a specific session.
     *
     * GET /api/feedback/session/{sessionId}
     *
     * @param sessionId the session ID
     * @return list of feedback records
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponseDTO<List<MentorFeedbackResponseDTO>>> getFeedbackBySession(
            @PathVariable("sessionId") Integer sessionId) {

        List<MentorFeedbackResponseDTO> feedbackList =
                mentorFeedbackService.getFeedbackBySession(sessionId);

        ApiResponseDTO<List<MentorFeedbackResponseDTO>> response = ApiResponseDTO.success(
                "Feedback retrieved successfully", feedbackList);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
