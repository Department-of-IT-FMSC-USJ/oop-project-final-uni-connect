package com.uniconnect.industry.modules.suggestions.entity;

import com.uniconnect.industry.modules.suggestions.enums.ReviewStatus;
import com.uniconnect.industry.modules.suggestions.enums.SuggestionCategory;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "curriculum_suggestions")
public class CurriculumSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suggestion_id")
    private Integer suggestionId;

    @Column(name = "mentor_id", nullable = false)
    private Integer mentorId;

    @Column(name = "suggestion_title", nullable = false, length = 120)
    private String suggestionTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private SuggestionCategory category;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "suggested_course", length = 100)
    private String suggestedCourse;

    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false)
    private ReviewStatus reviewStatus;

    public CurriculumSuggestion() {}

    @PrePersist
    protected void onCreate() {
        this.submissionDate = LocalDateTime.now();
        if (this.reviewStatus == null) {
            this.reviewStatus = ReviewStatus.PENDING;
        }
    }

    public Integer getSuggestionId() { return suggestionId; }
    public void setSuggestionId(Integer suggestionId) { this.suggestionId = suggestionId; }
    public Integer getMentorId() { return mentorId; }
    public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }
    public String getSuggestionTitle() { return suggestionTitle; }
    public void setSuggestionTitle(String suggestionTitle) { this.suggestionTitle = suggestionTitle; }
    public SuggestionCategory getCategory() { return category; }
    public void setCategory(SuggestionCategory category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSuggestedCourse() { return suggestedCourse; }
    public void setSuggestedCourse(String suggestedCourse) { this.suggestedCourse = suggestedCourse; }
    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }
    public ReviewStatus getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(ReviewStatus reviewStatus) { this.reviewStatus = reviewStatus; }

    @Override
    public String toString() {
        return "CurriculumSuggestion{suggestionId=" + suggestionId + ", mentorId=" + mentorId +
                ", suggestionTitle='" + suggestionTitle + "', category=" + category + '}';
    }
}
