package com.app.modules.points.dto;

import com.app.modules.points.enums.ActivityType;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for contribution points calculation request.
 */
public class ContributionPointsRequestDTO {

    @NotNull(message = "Participation ID is required")
    private Integer participationId;

    @NotNull(message = "Activity type is required")
    private ActivityType activityType;

    public ContributionPointsRequestDTO() {
    }

    public ContributionPointsRequestDTO(Integer participationId, ActivityType activityType) {
        this.participationId = participationId;
        this.activityType = activityType;
    }

    // Getters and Setters

    public Integer getParticipationId() {
        return participationId;
    }

    public void setParticipationId(Integer participationId) {
        this.participationId = participationId;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    @Override
    public String toString() {
        return "ContributionPointsRequestDTO{" +
                "participationId=" + participationId +
                ", activityType=" + activityType +
                '}';
    }
}
