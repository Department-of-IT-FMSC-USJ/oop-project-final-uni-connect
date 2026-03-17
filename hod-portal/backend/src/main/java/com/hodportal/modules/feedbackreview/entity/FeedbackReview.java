package com.hodportal.modules.feedbackreview.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class mapped to the 'feedback_reviews' database table.
 */
@Entity
@Table(name = "feedback_reviews")
public class FeedbackReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;

    @Column(name = "session_id", nullable = false)
    private Integer sessionId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "mentor_name", length = 150)
    private String mentorName;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "feedback_comment", columnDefinition = "TEXT")
    private String feedbackComment;

    @Column(name = "submitted_date", nullable = false)
    private LocalDateTime submittedDate;

    public FeedbackReview() {
    }

    public FeedbackReview(Integer feedbackId, Integer sessionId, Integer studentId,
                           String mentorName, Integer rating, String feedbackComment,
                           LocalDateTime submittedDate) {
        this.feedbackId = feedbackId;
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.mentorName = mentorName;
        this.rating = rating;
        this.feedbackComment = feedbackComment;
        this.submittedDate = submittedDate;
    }

    @PrePersist
    protected void onCreate() {
        if (this.submittedDate == null) {
            this.submittedDate = LocalDateTime.now();
        }
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

    @Override
    public String toString() {
        return "FeedbackReview{" +
                "feedbackId=" + feedbackId +
                ", sessionId=" + sessionId +
                ", studentId=" + studentId +
                ", mentorName='" + mentorName + '\'' +
                ", rating=" + rating +
                ", feedbackComment='" + feedbackComment + '\'' +
                ", submittedDate=" + submittedDate +
                '}';
    }
}
