package com.uniconnect.hod.modules.contributionreview.dto;

import java.time.LocalDateTime;

/**
 * DTO for contribution review response.
 */
public class ContributionReviewResponseDTO {

    private Integer reviewId;
    private Integer contributionId;
    private Integer studentId;
    private String flaggedReason;
    private String evidenceFilePath;
    private Integer hodId;
    private String decisionStatus;
    private String reviewComment;
    private LocalDateTime decisionDate;
    private LocalDateTime createdAt;

    public ContributionReviewResponseDTO() {
    }

    public ContributionReviewResponseDTO(Integer reviewId, Integer contributionId, Integer studentId,
                                          String flaggedReason, String evidenceFilePath, Integer hodId,
                                          String decisionStatus, String reviewComment,
                                          LocalDateTime decisionDate, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.contributionId = contributionId;
        this.studentId = studentId;
        this.flaggedReason = flaggedReason;
        this.evidenceFilePath = evidenceFilePath;
        this.hodId = hodId;
        this.decisionStatus = decisionStatus;
        this.reviewComment = reviewComment;
        this.decisionDate = decisionDate;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }
    public Integer getContributionId() { return contributionId; }
    public void setContributionId(Integer contributionId) { this.contributionId = contributionId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getFlaggedReason() { return flaggedReason; }
    public void setFlaggedReason(String flaggedReason) { this.flaggedReason = flaggedReason; }
    public String getEvidenceFilePath() { return evidenceFilePath; }
    public void setEvidenceFilePath(String evidenceFilePath) { this.evidenceFilePath = evidenceFilePath; }
    public Integer getHodId() { return hodId; }
    public void setHodId(Integer hodId) { this.hodId = hodId; }
    public String getDecisionStatus() { return decisionStatus; }
    public void setDecisionStatus(String decisionStatus) { this.decisionStatus = decisionStatus; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public LocalDateTime getDecisionDate() { return decisionDate; }
    public void setDecisionDate(LocalDateTime decisionDate) { this.decisionDate = decisionDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "ContributionReviewResponseDTO{" +
                "reviewId=" + reviewId +
                ", contributionId=" + contributionId +
                ", studentId=" + studentId +
                ", flaggedReason='" + flaggedReason + '\'' +
                ", evidenceFilePath='" + evidenceFilePath + '\'' +
                ", hodId=" + hodId +
                ", decisionStatus='" + decisionStatus + '\'' +
                ", reviewComment='" + reviewComment + '\'' +
                ", decisionDate=" + decisionDate +
                ", createdAt=" + createdAt +
                '}';
    }

    // Static Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer reviewId;
        private Integer contributionId;
        private Integer studentId;
        private String flaggedReason;
        private String evidenceFilePath;
        private Integer hodId;
        private String decisionStatus;
        private String reviewComment;
        private LocalDateTime decisionDate;
        private LocalDateTime createdAt;

        public Builder reviewId(Integer reviewId) { this.reviewId = reviewId; return this; }
        public Builder contributionId(Integer contributionId) { this.contributionId = contributionId; return this; }
        public Builder studentId(Integer studentId) { this.studentId = studentId; return this; }
        public Builder flaggedReason(String flaggedReason) { this.flaggedReason = flaggedReason; return this; }
        public Builder evidenceFilePath(String evidenceFilePath) { this.evidenceFilePath = evidenceFilePath; return this; }
        public Builder hodId(Integer hodId) { this.hodId = hodId; return this; }
        public Builder decisionStatus(String decisionStatus) { this.decisionStatus = decisionStatus; return this; }
        public Builder reviewComment(String reviewComment) { this.reviewComment = reviewComment; return this; }
        public Builder decisionDate(LocalDateTime decisionDate) { this.decisionDate = decisionDate; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ContributionReviewResponseDTO build() {
            return new ContributionReviewResponseDTO(reviewId, contributionId, studentId,
                    flaggedReason, evidenceFilePath, hodId, decisionStatus, reviewComment,
                    decisionDate, createdAt);
        }
    }
}
