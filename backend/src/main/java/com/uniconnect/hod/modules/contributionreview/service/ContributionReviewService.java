package com.uniconnect.hod.modules.contributionreview.service;

import com.uniconnect.hod.modules.contributionreview.dto.ContributionReviewRequestDTO;
import com.uniconnect.hod.modules.contributionreview.dto.ContributionReviewResponseDTO;

import java.util.List;

/**
 * Service interface for Contribution Review operations.
 */
public interface ContributionReviewService {

    /**
     * Get all contribution review records.
     *
     * @return list of contribution review records
     */
    List<ContributionReviewResponseDTO> getAllContributionReviews();

    /**
     * Search contribution reviews by student GBM (registrationNumber).
     */
    List<ContributionReviewResponseDTO> getContributionReviewsByGBM(String gbm);

    /**
     * Finalize the HoD's decision for a flagged contribution record.
     *
     * @param reviewId the review ID
     * @param hodId the HoD user ID
     * @param requestDTO the decision details
     * @return the updated review record
     */
    ContributionReviewResponseDTO finalizeDecision(Integer reviewId, Integer hodId,
                                                    ContributionReviewRequestDTO requestDTO);
}
