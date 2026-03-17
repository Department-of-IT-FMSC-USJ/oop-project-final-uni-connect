package com.hodportal.modules.contributionreview.controller;

import com.hodportal.common.dto.ApiResponseDTO;
import com.hodportal.common.exception.UnauthorizedAccessException;
import com.hodportal.modules.contributionreview.dto.ContributionReviewRequestDTO;
import com.hodportal.modules.contributionreview.dto.ContributionReviewResponseDTO;
import com.hodportal.modules.contributionreview.service.ContributionReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Contribution Review operations.
 * Only HoD users can access these endpoints.
 */
@RestController
@RequestMapping("/api/contribution-reviews")
public class ContributionReviewController {

    private final ContributionReviewService contributionReviewService;

    public ContributionReviewController(ContributionReviewService contributionReviewService) {
        this.contributionReviewService = contributionReviewService;
    }

    /**
     * Get all contribution review records.
     *
     * GET /api/contribution-reviews
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ContributionReviewResponseDTO>>> getAllContributionReviews(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole) {

        if (!"HOD".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only HoD users can access contribution review records.");
        }

        List<ContributionReviewResponseDTO> reviews = contributionReviewService.getAllContributionReviews();

        ApiResponseDTO<List<ContributionReviewResponseDTO>> response = ApiResponseDTO.success(
                "Contribution reviews retrieved successfully", reviews);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Finalize the HoD's decision for a flagged contribution record.
     *
     * PUT /api/contribution-reviews/{reviewId}/decide
     */
    @PutMapping("/{reviewId}/decide")
    public ResponseEntity<ApiResponseDTO<ContributionReviewResponseDTO>> finalizeDecision(
            @PathVariable("reviewId") Integer reviewId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") Integer hodId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole,
            @Valid @RequestBody ContributionReviewRequestDTO requestDTO) {

        if (!"HOD".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only HoD users can finalize contribution review decisions.");
        }

        ContributionReviewResponseDTO responseDTO = contributionReviewService.finalizeDecision(
                reviewId, hodId, requestDTO);

        ApiResponseDTO<ContributionReviewResponseDTO> response = ApiResponseDTO.success(
                "Contribution review decision finalized successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
