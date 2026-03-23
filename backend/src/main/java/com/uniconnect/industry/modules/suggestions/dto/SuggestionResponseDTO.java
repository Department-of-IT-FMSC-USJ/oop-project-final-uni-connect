package com.uniconnect.industry.modules.suggestions.dto;

import java.time.LocalDateTime;

public class SuggestionResponseDTO {

    private Integer suggestionId;
    private Integer mentorId;
    private String suggestionTitle;
    private String category;
    private String description;
    private String suggestedCourse;
    private LocalDateTime submissionDate;
    private String reviewStatus;

    public SuggestionResponseDTO() {}

    public SuggestionResponseDTO(Integer suggestionId, Integer mentorId, String suggestionTitle,
                                  String category, String description, String suggestedCourse,
                                  LocalDateTime submissionDate, String reviewStatus) {
        this.suggestionId = suggestionId;
        this.mentorId = mentorId;
        this.suggestionTitle = suggestionTitle;
        this.category = category;
        this.description = description;
        this.suggestedCourse = suggestedCourse;
        this.submissionDate = submissionDate;
        this.reviewStatus = reviewStatus;
    }

    public Integer getSuggestionId() { return suggestionId; }
    public void setSuggestionId(Integer suggestionId) { this.suggestionId = suggestionId; }
    public Integer getMentorId() { return mentorId; }
    public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }
    public String getSuggestionTitle() { return suggestionTitle; }
    public void setSuggestionTitle(String suggestionTitle) { this.suggestionTitle = suggestionTitle; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSuggestedCourse() { return suggestedCourse; }
    public void setSuggestedCourse(String suggestedCourse) { this.suggestedCourse = suggestedCourse; }
    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer suggestionId;
        private Integer mentorId;
        private String suggestionTitle;
        private String category;
        private String description;
        private String suggestedCourse;
        private LocalDateTime submissionDate;
        private String reviewStatus;

        public Builder suggestionId(Integer v) { this.suggestionId = v; return this; }
        public Builder mentorId(Integer v) { this.mentorId = v; return this; }
        public Builder suggestionTitle(String v) { this.suggestionTitle = v; return this; }
        public Builder category(String v) { this.category = v; return this; }
        public Builder description(String v) { this.description = v; return this; }
        public Builder suggestedCourse(String v) { this.suggestedCourse = v; return this; }
        public Builder submissionDate(LocalDateTime v) { this.submissionDate = v; return this; }
        public Builder reviewStatus(String v) { this.reviewStatus = v; return this; }

        public SuggestionResponseDTO build() {
            return new SuggestionResponseDTO(suggestionId, mentorId, suggestionTitle,
                    category, description, suggestedCourse, submissionDate, reviewStatus);
        }
    }
}
