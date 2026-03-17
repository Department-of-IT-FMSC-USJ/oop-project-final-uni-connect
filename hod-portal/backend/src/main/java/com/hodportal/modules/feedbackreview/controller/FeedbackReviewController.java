package com.hodportal.modules.feedbackreview.controller;

import com.hodportal.common.dto.ApiResponseDTO;
import com.hodportal.common.exception.UnauthorizedAccessException;
import com.hodportal.modules.feedbackreview.dto.FeedbackResponseDTO;
import com.hodportal.modules.feedbackreview.service.FeedbackReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Feedback Review operations.
 * Only HoD users can access feedback reports.
 */
@RestController
@RequestMapping("/api/feedback-reviews")
public class FeedbackReviewController {

    private final FeedbackReviewService feedbackReviewService;

    public FeedbackReviewController(FeedbackReviewService feedbackReviewService) {
        this.feedbackReviewService = feedbackReviewService;
    }

    /**
     * Get all feedback review records.
     *
     * GET /api/feedback-reviews
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<FeedbackResponseDTO>>> getAllFeedbackReviews(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole) {

        if (!"HOD".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only HoD users can access feedback reports.");
        }

        List<FeedbackResponseDTO> feedbacks = feedbackReviewService.getAllFeedbackReviews();

        ApiResponseDTO<List<FeedbackResponseDTO>> response = ApiResponseDTO.success(
                "Feedback reviews retrieved successfully", feedbacks);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
