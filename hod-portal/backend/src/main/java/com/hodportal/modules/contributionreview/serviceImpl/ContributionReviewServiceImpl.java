package com.hodportal.modules.contributionreview.serviceImpl;

import com.hodportal.common.exception.DecisionAlreadyMadeException;
import com.hodportal.common.exception.ResourceNotFoundException;
import com.hodportal.modules.contributionreview.dto.ContributionReviewRequestDTO;
import com.hodportal.modules.contributionreview.dto.ContributionReviewResponseDTO;
import com.hodportal.modules.contributionreview.entity.ContributionReview;
import com.hodportal.modules.contributionreview.enums.DecisionStatus;
import com.hodportal.modules.contributionreview.repository.ContributionReviewRepository;
import com.hodportal.modules.contributionreview.service.ContributionReviewService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ContributionReviewService.
 * Handles business logic for contribution review decisions.
 */
@Service
public class ContributionReviewServiceImpl implements ContributionReviewService {

    private final ContributionReviewRepository contributionReviewRepository;

    public ContributionReviewServiceImpl(ContributionReviewRepository contributionReviewRepository) {
        this.contributionReviewRepository = contributionReviewRepository;
    }

    /**
     * Get all contribution review records.
     */
    @Override
    public List<ContributionReviewResponseDTO> getAllContributionReviews() {
        List<ContributionReview> reviews = contributionReviewRepository.findAllByOrderByCreatedAtDesc();

        return reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finalize the HoD's decision for a flagged contribution record.
     * Prevents further modification after a final decision has been made.
     */
    @Override
    public ContributionReviewResponseDTO finalizeDecision(Integer reviewId, Integer hodId,
                                                           ContributionReviewRequestDTO requestDTO) {
        ContributionReview review = contributionReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contribution review not found with ID: " + reviewId));

        // Prevent re-modification after final decision
        if (review.getDecisionStatus() != null) {
            throw new DecisionAlreadyMadeException(
                    "A final decision has already been made for this contribution review. " +
                    "No further modifications are allowed.");
        }

        // Parse decision status
        DecisionStatus status;
        try {
            status = DecisionStatus.valueOf(requestDTO.getDecisionStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid decision status: " + requestDTO.getDecisionStatus() +
                    ". Must be APPROVED or REJECTED.");
        }

        // Validate review comment is provided
        if (requestDTO.getReviewComment() == null || requestDTO.getReviewComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Review comment is required before closing the review.");
        }

        review.setDecisionStatus(status);
        review.setReviewComment(requestDTO.getReviewComment());
        review.setHodId(hodId);
        review.setDecisionDate(LocalDateTime.now());

        ContributionReview saved = contributionReviewRepository.save(review);

        return mapToResponseDTO(saved);
    }

    /**
     * Map ContributionReview entity to ContributionReviewResponseDTO.
     */
    private ContributionReviewResponseDTO mapToResponseDTO(ContributionReview review) {
        return ContributionReviewResponseDTO.builder()
                .reviewId(review.getReviewId())
                .contributionId(review.getContributionId())
                .studentId(review.getStudentId())
                .flaggedReason(review.getFlaggedReason())
                .evidenceFilePath(review.getEvidenceFilePath())
                .hodId(review.getHodId())
                .decisionStatus(review.getDecisionStatus() != null ? review.getDecisionStatus().name() : null)
                .reviewComment(review.getReviewComment())
                .decisionDate(review.getDecisionDate())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
