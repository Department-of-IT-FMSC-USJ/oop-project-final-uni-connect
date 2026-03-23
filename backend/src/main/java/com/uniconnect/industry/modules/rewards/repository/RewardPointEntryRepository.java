package com.uniconnect.industry.modules.rewards.repository;

import com.uniconnect.industry.modules.rewards.entity.RewardPointEntry;
import com.uniconnect.industry.modules.rewards.enums.ContributionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardPointEntryRepository extends JpaRepository<RewardPointEntry, Integer> {

    List<RewardPointEntry> findByMentorIdOrderByUpdateDateDesc(Integer mentorId);

    boolean existsByMentorIdAndContributionTypeAndContributionReferenceId(
            Integer mentorId, ContributionType contributionType, Integer contributionReferenceId);

    @Query("SELECT COALESCE(SUM(r.pointsAwarded), 0) FROM RewardPointEntry r WHERE r.mentorId = :mentorId")
    Integer getTotalPointsByMentorId(@Param("mentorId") Integer mentorId);

    @Query("SELECT COALESCE(SUM(r.pointsAwarded), 0) FROM RewardPointEntry r WHERE r.mentorId = :mentorId AND r.contributionType = :type")
    Integer getPointsByMentorIdAndType(@Param("mentorId") Integer mentorId, @Param("type") ContributionType type);
}
