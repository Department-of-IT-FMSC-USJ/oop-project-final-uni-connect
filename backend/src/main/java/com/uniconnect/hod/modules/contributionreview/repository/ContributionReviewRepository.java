package com.uniconnect.hod.modules.contributionreview.repository;

import com.uniconnect.hod.modules.contributionreview.entity.ContributionReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ContributionReview entity.
 */
@Repository
public interface ContributionReviewRepository extends JpaRepository<ContributionReview, Integer> {

    /**
     * Find all contribution reviews ordered by creation date descending.
     */
    List<ContributionReview> findAllByOrderByCreatedAtDesc();

    /**
     * Find contribution reviews for a specific student ordered by creation date descending.
     */
    List<ContributionReview> findByStudentIdOrderByCreatedAtDesc(Integer studentId);
}
