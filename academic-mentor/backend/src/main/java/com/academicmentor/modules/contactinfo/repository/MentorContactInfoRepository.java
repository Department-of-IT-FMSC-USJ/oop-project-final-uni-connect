package com.academicmentor.modules.contactinfo.repository;

import com.academicmentor.modules.contactinfo.entity.MentorContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for MentorContactInfo entity.
 */
@Repository
public interface MentorContactInfoRepository extends JpaRepository<MentorContactInfo, Integer> {

    /**
     * Find contact info by mentor ID.
     */
    Optional<MentorContactInfo> findByMentorId(Integer mentorId);
}
