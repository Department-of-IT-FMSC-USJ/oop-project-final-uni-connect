package com.industrymentor.modules.rewards.dto;

import java.util.List;

public class RewardSummaryDTO {

    private Integer mentorId;
    private Integer totalRewardPoints;
    private Integer sessionPoints;
    private Integer suggestionPoints;
    private List<RewardPointResponseDTO> contributionHistory;

    public RewardSummaryDTO() {}

    public RewardSummaryDTO(Integer mentorId, Integer totalRewardPoints,
                             Integer sessionPoints, Integer suggestionPoints,
                             List<RewardPointResponseDTO> contributionHistory) {
        this.mentorId = mentorId;
        this.totalRewardPoints = totalRewardPoints;
        this.sessionPoints = sessionPoints;
        this.suggestionPoints = suggestionPoints;
        this.contributionHistory = contributionHistory;
    }

    public Integer getMentorId() { return mentorId; }
    public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }
    public Integer getTotalRewardPoints() { return totalRewardPoints; }
    public void setTotalRewardPoints(Integer totalRewardPoints) { this.totalRewardPoints = totalRewardPoints; }
    public Integer getSessionPoints() { return sessionPoints; }
    public void setSessionPoints(Integer sessionPoints) { this.sessionPoints = sessionPoints; }
    public Integer getSuggestionPoints() { return suggestionPoints; }
    public void setSuggestionPoints(Integer suggestionPoints) { this.suggestionPoints = suggestionPoints; }
    public List<RewardPointResponseDTO> getContributionHistory() { return contributionHistory; }
    public void setContributionHistory(List<RewardPointResponseDTO> contributionHistory) { this.contributionHistory = contributionHistory; }
}
