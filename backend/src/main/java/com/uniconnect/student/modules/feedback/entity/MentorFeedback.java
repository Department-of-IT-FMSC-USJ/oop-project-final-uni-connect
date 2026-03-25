package com.uniconnect.student.modules.feedback.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class mapped to the 'mentor_feedback' database table.
 */
@Entity
@Table(name = "mentor_feedback")
public class MentorFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;

    @Column(name = "session_id", nullable = false)
    private Integer sessionId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "feedback_comment", columnDefinition = "TEXT")
    private String feedbackComment;

    @Column(name = "submitted_date", nullable = false)
    private LocalDateTime submittedDate;

    public MentorFeedback() {
    }

    public MentorFeedback(Integer feedbackId, Integer sessionId, Integer studentId,
                           Integer rating, String feedbackComment, LocalDateTime submittedDate) {
        this.feedbackId = feedbackId;
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.rating = rating;
        this.feedbackComment = feedbackComment;
        this.submittedDate = submittedDate;
    }

    @PrePersist
    protected void onCreate() {
        this.submittedDate = LocalDateTime.now();
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
        return "MentorFeedback{" +
                "feedbackId=" + feedbackId +
                ", sessionId=" + sessionId +
                ", studentId=" + studentId +
                ", rating=" + rating +
                ", feedbackComment='" + feedbackComment + '\'' +
                ", submittedDate=" + submittedDate +
                '}';
    }
}
