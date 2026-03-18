package com.hodportal.modules.contributionreview.service;

import com.hodportal.modules.contributionreview.dto.ContributionReviewRequestDTO;
import com.hodportal.modules.contributionreview.dto.ContributionReviewResponseDTO;

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
