package com.uniconnect.student.modules.recommendation.repository;

import com.uniconnect.student.modules.recommendation.entity.MentorRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for MentorRecommendation entity.
 */
@Repository
public interface MentorRecommendationRepository extends JpaRepository<MentorRecommendation, Integer> {

    /**
     * Find all recommendations for a student, sorted by match score descending.
     */
    List<MentorRecommendation> findByStudentIdOrderByMatchScoreDesc(Integer studentId);

    /**
     * Check if a recommendation already exists for this student-mentor pair.
     */
    boolean existsByStudentIdAndMentorId(Integer studentId, Integer mentorId);

    /**
     * Delete all recommendations for a student (used when regenerating).
     */
    void deleteByStudentId(Integer studentId);
}
