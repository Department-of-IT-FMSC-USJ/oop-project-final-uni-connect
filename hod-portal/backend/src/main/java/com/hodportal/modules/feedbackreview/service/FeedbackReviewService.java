package com.hodportal.modules.feedbackreview.service;

import com.hodportal.modules.feedbackreview.dto.FeedbackResponseDTO;

import java.util.List;

/**
 * Service interface for Feedback Review operations.
 */
public interface FeedbackReviewService {

    /**
     * Get all feedback review records for HoD review.
     *
     * @return list of feedback records
     */
    List<FeedbackResponseDTO> getAllFeedbackReviews();
}
