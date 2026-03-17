package com.industrymentor.modules.suggestions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SuggestionRequestDTO {

    @NotNull(message = "Mentor ID is required")
    private Integer mentorId;

    @NotBlank(message = "Suggestion title is required")
    @Size(min = 5, max = 120, message = "Suggestion title must be between 5 and 120 characters")
    private String suggestionTitle;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Size(max = 100, message = "Suggested course cannot exceed 100 characters")
    private String suggestedCourse;

    public SuggestionRequestDTO() {}

    public SuggestionRequestDTO(Integer mentorId, String suggestionTitle, String category,
                                 String description, String suggestedCourse) {
        this.mentorId = mentorId;
        this.suggestionTitle = suggestionTitle;
        this.category = category;
        this.description = description;
        this.suggestedCourse = suggestedCourse;
    }

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
}
