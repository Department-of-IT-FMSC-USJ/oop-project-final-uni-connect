package com.uniconnect.industry.modules.rewards.dto;

import java.time.LocalDateTime;

public class RewardPointResponseDTO {

    private Integer id;
    private Integer mentorId;
    private String contributionType;
    private Integer pointsAwarded;
    private Integer contributionReferenceId;
    private Integer updatedTotalPoints;
    private LocalDateTime updateDate;

    public RewardPointResponseDTO() {}

    public RewardPointResponseDTO(Integer id, Integer mentorId, String contributionType,
                                   Integer pointsAwarded, Integer contributionReferenceId,
                                   Integer updatedTotalPoints, LocalDateTime updateDate) {
        this.id = id;
        this.mentorId = mentorId;
        this.contributionType = contributionType;
        this.pointsAwarded = pointsAwarded;
        this.contributionReferenceId = contributionReferenceId;
        this.updatedTotalPoints = updatedTotalPoints;
        this.updateDate = updateDate;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getMentorId() { return mentorId; }
    public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }
    public String getContributionType() { return contributionType; }
    public void setContributionType(String contributionType) { this.contributionType = contributionType; }
    public Integer getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(Integer pointsAwarded) { this.pointsAwarded = pointsAwarded; }
    public Integer getContributionReferenceId() { return contributionReferenceId; }
    public void setContributionReferenceId(Integer contributionReferenceId) { this.contributionReferenceId = contributionReferenceId; }
    public Integer getUpdatedTotalPoints() { return updatedTotalPoints; }
    public void setUpdatedTotalPoints(Integer updatedTotalPoints) { this.updatedTotalPoints = updatedTotalPoints; }
    public LocalDateTime getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer id;
        private Integer mentorId;
        private String contributionType;
        private Integer pointsAwarded;
        private Integer contributionReferenceId;
        private Integer updatedTotalPoints;
        private LocalDateTime updateDate;

        public Builder id(Integer v) { this.id = v; return this; }
        public Builder mentorId(Integer v) { this.mentorId = v; return this; }
        public Builder contributionType(String v) { this.contributionType = v; return this; }
        public Builder pointsAwarded(Integer v) { this.pointsAwarded = v; return this; }
        public Builder contributionReferenceId(Integer v) { this.contributionReferenceId = v; return this; }
        public Builder updatedTotalPoints(Integer v) { this.updatedTotalPoints = v; return this; }
        public Builder updateDate(LocalDateTime v) { this.updateDate = v; return this; }

        public RewardPointResponseDTO build() {
            return new RewardPointResponseDTO(id, mentorId, contributionType, pointsAwarded,
                    contributionReferenceId, updatedTotalPoints, updateDate);
        }
    }
}
