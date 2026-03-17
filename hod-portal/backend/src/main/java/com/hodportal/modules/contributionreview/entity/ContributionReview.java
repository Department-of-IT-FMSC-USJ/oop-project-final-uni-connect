package com.hodportal.modules.contributionreview.entity;

import com.hodportal.modules.contributionreview.enums.DecisionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class mapped to the 'contribution_reviews' database table.
 */
@Entity
@Table(name = "contribution_reviews")
public class ContributionReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @Column(name = "contribution_id", nullable = false)
    private Integer contributionId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "flagged_reason", columnDefinition = "TEXT")
    private String flaggedReason;

    @Column(name = "evidence_file_path", length = 500)
    private String evidenceFilePath;

    @Column(name = "hod_id")
    private Integer hodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_status")
    private DecisionStatus decisionStatus;

    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    @Column(name = "decision_date")
    private LocalDateTime decisionDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ContributionReview() {
    }

    public ContributionReview(Integer reviewId, Integer contributionId, Integer studentId,
                               String flaggedReason, String evidenceFilePath, Integer hodId,
                               DecisionStatus decisionStatus, String reviewComment,
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

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Integer getReviewId() {
        return reviewId;
    }

    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    public Integer getContributionId() {
        return contributionId;
    }

    public void setContributionId(Integer contributionId) {
        this.contributionId = contributionId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getFlaggedReason() {
        return flaggedReason;
    }

    public void setFlaggedReason(String flaggedReason) {
        this.flaggedReason = flaggedReason;
    }

    public String getEvidenceFilePath() {
        return evidenceFilePath;
    }

    public void setEvidenceFilePath(String evidenceFilePath) {
        this.evidenceFilePath = evidenceFilePath;
    }

    public Integer getHodId() {
        return hodId;
    }

    public void setHodId(Integer hodId) {
        this.hodId = hodId;
    }

    public DecisionStatus getDecisionStatus() {
        return decisionStatus;
    }

    public void setDecisionStatus(DecisionStatus decisionStatus) {
        this.decisionStatus = decisionStatus;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public LocalDateTime getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(LocalDateTime decisionDate) {
        this.decisionDate = decisionDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ContributionReview{" +
                "reviewId=" + reviewId +
                ", contributionId=" + contributionId +
                ", studentId=" + studentId +
                ", flaggedReason='" + flaggedReason + '\'' +
                ", evidenceFilePath='" + evidenceFilePath + '\'' +
                ", hodId=" + hodId +
                ", decisionStatus=" + decisionStatus +
                ", reviewComment='" + reviewComment + '\'' +
                ", decisionDate=" + decisionDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
