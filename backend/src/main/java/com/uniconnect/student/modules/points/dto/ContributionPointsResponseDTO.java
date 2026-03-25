package com.uniconnect.student.modules.points.dto;

import com.uniconnect.student.modules.points.enums.ActivityType;

import java.time.LocalDateTime;

/**
 * DTO for contribution points response.
 */
public class ContributionPointsResponseDTO {

    private Integer pointId;
    private Integer studentId;
    private Integer pointsEarned;
    private ActivityType activityType;
    private Integer activityReferenceId;
    private Integer updatedTotalPoints;
    private LocalDateTime createdDate;

    public ContributionPointsResponseDTO() {
    }

    public ContributionPointsResponseDTO(Integer pointId, Integer studentId, Integer pointsEarned,
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
        return "ContributionPointsResponseDTO{" +
                "pointId=" + pointId +
                ", studentId=" + studentId +
                ", pointsEarned=" + pointsEarned +
                ", activityType=" + activityType +
                ", activityReferenceId=" + activityReferenceId +
                ", updatedTotalPoints=" + updatedTotalPoints +
                ", createdDate=" + createdDate +
                '}';
    }

    // Static Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer pointId;
        private Integer studentId;
        private Integer pointsEarned;
        private ActivityType activityType;
        private Integer activityReferenceId;
        private Integer updatedTotalPoints;
        private LocalDateTime createdDate;

        public Builder pointId(Integer pointId) {
            this.pointId = pointId;
            return this;
        }

        public Builder studentId(Integer studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder pointsEarned(Integer pointsEarned) {
            this.pointsEarned = pointsEarned;
            return this;
        }

        public Builder activityType(ActivityType activityType) {
            this.activityType = activityType;
            return this;
        }

        public Builder activityReferenceId(Integer activityReferenceId) {
            this.activityReferenceId = activityReferenceId;
            return this;
        }

        public Builder updatedTotalPoints(Integer updatedTotalPoints) {
            this.updatedTotalPoints = updatedTotalPoints;
            return this;
        }

        public Builder createdDate(LocalDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public ContributionPointsResponseDTO build() {
            return new ContributionPointsResponseDTO(
                    pointId, studentId, pointsEarned, activityType,
                    activityReferenceId, updatedTotalPoints, createdDate);
        }
    }
}
