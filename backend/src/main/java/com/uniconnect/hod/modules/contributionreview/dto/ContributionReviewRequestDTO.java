package com.uniconnect.hod.modules.contributionreview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for contribution review decision request.
 */
public class ContributionReviewRequestDTO {

    @NotBlank(message = "Decision status is required")
    private String decisionStatus;

    @Size(max = 300, message = "Review comment cannot exceed 300 characters")
    private String reviewComment;

    public ContributionReviewRequestDTO() {
    }

    public ContributionReviewRequestDTO(String decisionStatus, String reviewComment) {
        this.decisionStatus = decisionStatus;
        this.reviewComment = reviewComment;
    }

    // Getters and Setters

    public String getDecisionStatus() {
        return decisionStatus;
    }

    public void setDecisionStatus(String decisionStatus) {
        this.decisionStatus = decisionStatus;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    @Override
    public String toString() {
        return "ContributionReviewRequestDTO{" +
                "decisionStatus='" + decisionStatus + '\'' +
                ", reviewComment='" + reviewComment + '\'' +
                '}';
    }
}
