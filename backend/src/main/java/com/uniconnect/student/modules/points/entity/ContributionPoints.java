package com.uniconnect.student.modules.points.entity;

import com.uniconnect.student.modules.points.enums.ActivityType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class mapped to the 'contribution_points' database table.
 */
@Entity
@Table(name = "contribution_points")
public class ContributionPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Integer pointId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "activity_reference_id", nullable = false)
    private Integer activityReferenceId;

    @Column(name = "updated_total_points", nullable = false)
    private Integer updatedTotalPoints;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    public ContributionPoints() {
    }

    public ContributionPoints(Integer pointId, Integer studentId, Integer pointsEarned,
                               ActivityType activityType, Integer activityReferenceId,
                               Integer updatedTotalPoints, LocalDateTime createdDate) {
        this.pointId = pointId;
        this.studentId = studentId;
        this.pointsEarned = pointsEarned;
        this.activityType = activityType;
        this.activityReferenceId = activityReferenceId;
        this.updatedTotalPoints = updatedTotalPoints;
        this.createdDate = createdDate;
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    // Getters and Setters

    public Integer getPointId() {
        return pointId;
    }

    public void setPointId(Integer pointId) {
        this.pointId = pointId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public Integer getActivityReferenceId() {
        return activityReferenceId;
    }

    public void setActivityReferenceId(Integer activityReferenceId) {
        this.activityReferenceId = activityReferenceId;
    }

    public Integer getUpdatedTotalPoints() {
        return updatedTotalPoints;
    }

    public void setUpdatedTotalPoints(Integer updatedTotalPoints) {
        this.updatedTotalPoints = updatedTotalPoints;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "ContributionPoints{" +
                "pointId=" + pointId +
                ", studentId=" + studentId +
                ", pointsEarned=" + pointsEarned +
                ", activityType=" + activityType +
                ", activityReferenceId=" + activityReferenceId +
                ", updatedTotalPoints=" + updatedTotalPoints +
                ", createdDate=" + createdDate +
                '}';
    }
}
