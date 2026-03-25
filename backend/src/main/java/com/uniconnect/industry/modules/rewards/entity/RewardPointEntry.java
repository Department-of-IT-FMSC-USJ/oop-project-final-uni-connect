package com.uniconnect.industry.modules.rewards.entity;

import com.uniconnect.industry.modules.rewards.enums.ContributionType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward_point_entries")
public class RewardPointEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "mentor_id", nullable = false)
    private Integer mentorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "contribution_type", nullable = false)
    private ContributionType contributionType;

    @Column(name = "points_awarded", nullable = false)
    private Integer pointsAwarded;

    @Column(name = "contribution_reference_id", nullable = false)
    private Integer contributionReferenceId;

    @Column(name = "updated_total_points", nullable = false)
    private Integer updatedTotalPoints;

    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;

    public RewardPointEntry() {}

    @PrePersist
    protected void onCreate() {
        this.updateDate = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getMentorId() { return mentorId; }
    public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }
    public ContributionType getContributionType() { return contributionType; }
    public void setContributionType(ContributionType contributionType) { this.contributionType = contributionType; }
    public Integer getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(Integer pointsAwarded) { this.pointsAwarded = pointsAwarded; }
    public Integer getContributionReferenceId() { return contributionReferenceId; }
    public void setContributionReferenceId(Integer contributionReferenceId) { this.contributionReferenceId = contributionReferenceId; }
    public Integer getUpdatedTotalPoints() { return updatedTotalPoints; }
    public void setUpdatedTotalPoints(Integer updatedTotalPoints) { this.updatedTotalPoints = updatedTotalPoints; }
    public LocalDateTime getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }

    @Override
    public String toString() {
        return "RewardPointEntry{id=" + id + ", mentorId=" + mentorId +
                ", contributionType=" + contributionType + ", pointsAwarded=" + pointsAwarded + '}';
    }
}
