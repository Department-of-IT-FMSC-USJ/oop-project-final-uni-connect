package com.hodportal.modules.feedbackreview.repository;

import com.hodportal.modules.feedbackreview.entity.FeedbackReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for FeedbackReview entity.
 */
@Repository
public interface FeedbackReviewRepository extends JpaRepository<FeedbackReview, Integer> {

    /**
     * Find all feedback reviews ordered by submitted date descending.
     */
    List<FeedbackReview> findAllByOrderBySubmittedDateDesc();
}
