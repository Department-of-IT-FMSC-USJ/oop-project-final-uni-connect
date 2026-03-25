package com.uniconnect.student.modules.recommendation.repository;

import com.uniconnect.student.modules.recommendation.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MentorProfile entity.
 */
@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Integer> {

    /**
     * Find a mentor profile by mentor ID.
     */
    Optional<MentorProfile> findByMentorId(Integer mentorId);

    /**
     * Check if a mentor profile exists.
     */
    boolean existsByMentorId(Integer mentorId);

    /**
     * Find all available mentor profiles.
     */
    List<MentorProfile> findByIsAvailableTrue();
}
