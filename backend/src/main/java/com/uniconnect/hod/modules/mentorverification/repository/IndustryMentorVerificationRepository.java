package com.uniconnect.hod.modules.mentorverification.repository;

import com.uniconnect.hod.modules.mentorverification.entity.IndustryMentorVerification;
import com.uniconnect.hod.modules.mentorverification.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for IndustryMentorVerification entity.
 */
@Repository
public interface IndustryMentorVerificationRepository extends JpaRepository<IndustryMentorVerification, Integer> {

    /**
     * Find all mentor verifications by status.
     */
    List<IndustryMentorVerification> findByVerificationStatus(VerificationStatus verificationStatus);

    /**
     * Find all mentor verifications ordered by creation date descending.
     */
    List<IndustryMentorVerification> findAllByOrderByCreatedAtDesc();
}
