package com.uniconnect.student.modules.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for mentor feedback submission request.
 */
public class MentorFeedbackRequestDTO {

    @NotNull(message = "Session ID is required")
    private Integer sessionId;

    @NotNull(message = "Student ID is required")
    private Integer studentId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 2000, message = "Feedback comment cannot exceed 2000 characters")
    private String feedbackComment;

    public MentorFeedbackRequestDTO() {
    }

    public MentorFeedbackRequestDTO(Integer sessionId, Integer studentId,
                                     Integer rating, String feedbackComment) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.rating = rating;
        this.feedbackComment = feedbackComment;
    }

    // Getters and Setters

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getFeedbackComment() {
        return feedbackComment;
    }

    public void setFeedbackComment(String feedbackComment) {
        this.feedbackComment = feedbackComment;
    }

    @Override
    public String toString() {
        return "MentorFeedbackRequestDTO{" +
                "sessionId=" + sessionId +
                ", studentId=" + studentId +
                ", rating=" + rating +
                ", feedbackComment='" + feedbackComment + '\'' +
                '}';
    }
}
