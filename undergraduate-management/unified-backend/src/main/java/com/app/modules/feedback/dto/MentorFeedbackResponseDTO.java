package com.app.modules.feedback.dto;

import java.time.LocalDateTime;

/**
 * DTO for mentor feedback response.
 */
public class MentorFeedbackResponseDTO {

    private Integer feedbackId;
    private Integer sessionId;
    private Integer studentId;
    private Integer rating;
    private String feedbackComment;
    private LocalDateTime submittedDate;

    public MentorFeedbackResponseDTO() {
    }

    public MentorFeedbackResponseDTO(Integer feedbackId, Integer sessionId, Integer studentId,
                                      Integer rating, String feedbackComment,
                                      LocalDateTime submittedDate) {
        this.feedbackId = feedbackId;
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.rating = rating;
        this.feedbackComment = feedbackComment;
        this.submittedDate = submittedDate;
    }

    // Getters and Setters

    public Integer getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Integer feedbackId) {
        this.feedbackId = feedbackId;
    }

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

    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }

    @Override
    public String toString() {
        return "MentorFeedbackResponseDTO{" +
                "feedbackId=" + feedbackId +
                ", sessionId=" + sessionId +
                ", studentId=" + studentId +
                ", rating=" + rating +
                ", feedbackComment='" + feedbackComment + '\'' +
                ", submittedDate=" + submittedDate +
                '}';
    }

    // Static Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer feedbackId;
        private Integer sessionId;
        private Integer studentId;
        private Integer rating;
        private String feedbackComment;
        private LocalDateTime submittedDate;

        public Builder feedbackId(Integer feedbackId) {
            this.feedbackId = feedbackId;
            return this;
        }

        public Builder sessionId(Integer sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder studentId(Integer studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder rating(Integer rating) {
            this.rating = rating;
            return this;
        }

        public Builder feedbackComment(String feedbackComment) {
            this.feedbackComment = feedbackComment;
            return this;
        }

        public Builder submittedDate(LocalDateTime submittedDate) {
            this.submittedDate = submittedDate;
            return this;
        }

        public MentorFeedbackResponseDTO build() {
            return new MentorFeedbackResponseDTO(
                    feedbackId, sessionId, studentId, rating,
                    feedbackComment, submittedDate);
        }
    }
}
