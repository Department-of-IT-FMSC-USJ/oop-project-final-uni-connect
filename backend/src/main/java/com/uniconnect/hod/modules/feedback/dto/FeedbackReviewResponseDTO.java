package com.uniconnect.hod.modules.feedback.dto;

import java.time.LocalDateTime;

/**
 * DTO returned to HoD feedback dashboard.
 */
public class FeedbackReviewResponseDTO {

    private Integer sessionId;
    private String mentorName;
    private Integer rating;
    private String feedbackComment;
    private LocalDateTime submittedDate;

    public FeedbackReviewResponseDTO() {
    }

    public FeedbackReviewResponseDTO(Integer sessionId,
                                       String mentorName,
                                       Integer rating,
                                       String feedbackComment,
                                       LocalDateTime submittedDate) {
        this.sessionId = sessionId;
        this.mentorName = mentorName;
        this.rating = rating;
        this.feedbackComment = feedbackComment;
        this.submittedDate = submittedDate;
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
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
}

