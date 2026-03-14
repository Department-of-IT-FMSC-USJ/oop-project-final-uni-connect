package com.app.modules.recommendation.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity to store computed mentor recommendations for students.
 * Mapped to the 'mentor_recommendations' database table.
 */
@Entity
@Table(name = "mentor_recommendations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "mentor_id"}))
public class MentorRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Integer recommendationId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "mentor_id", nullable = false)
    private Integer mentorId;

    @Column(name = "match_score", nullable = false)
    private Integer matchScore;

    @Column(name = "recommendation_date", nullable = false)
    private LocalDateTime recommendationDate;

    public MentorRecommendation() {
    }

    public MentorRecommendation(Integer recommendationId, Integer studentId, Integer mentorId,
                                 Integer matchScore, LocalDateTime recommendationDate) {
        this.recommendationId = recommendationId;
        this.studentId = studentId;
        this.mentorId = mentorId;
        this.matchScore = matchScore;
        this.recommendationDate = recommendationDate;
    }

    @PrePersist
    protected void onCreate() {
        this.recommendationDate = LocalDateTime.now();
    }

    // Getters and Setters

    public Integer getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(Integer recommendationId) {
        this.recommendationId = recommendationId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getMentorId() {
        return mentorId;
    }

    public void setMentorId(Integer mentorId) {
        this.mentorId = mentorId;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public LocalDateTime getRecommendationDate() {
        return recommendationDate;
    }

    public void setRecommendationDate(LocalDateTime recommendationDate) {
        this.recommendationDate = recommendationDate;
    }

    @Override
    public String toString() {
        return "MentorRecommendation{" +
                "recommendationId=" + recommendationId +
                ", studentId=" + studentId +
                ", mentorId=" + mentorId +
                ", matchScore=" + matchScore +
                ", recommendationDate=" + recommendationDate +
                '}';
    }
}
