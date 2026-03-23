package com.uniconnect.student.modules.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for event participation submission request.
 */
public class EventParticipationRequestDTO {

    @NotBlank(message = "Event name is required")
    @Size(min = 3, max = 150, message = "Event name must be between 3 and 150 characters")
    private String eventName;

    @NotBlank(message = "Event type is required")
    @Size(max = 50, message = "Event type cannot exceed 50 characters")
    private String eventType;

    @NotBlank(message = "Participation role is required")
    @Size(max = 50, message = "Participation role cannot exceed 50 characters")
    private String participationRole;

    @Size(max = 2000, message = "Achievement description cannot exceed 2000 characters")
    private String achievementDescription;

    public EventParticipationRequestDTO() {
    }

    public EventParticipationRequestDTO(String eventName, String eventType,
                                         String participationRole, String achievementDescription) {
        this.eventName = eventName;
        this.eventType = eventType;
        this.participationRole = participationRole;
        this.achievementDescription = achievementDescription;
    }

    // Getters and Setters

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getParticipationRole() {
        return participationRole;
    }

    public void setParticipationRole(String participationRole) {
        this.participationRole = participationRole;
    }

    public String getAchievementDescription() {
        return achievementDescription;
    }

    public void setAchievementDescription(String achievementDescription) {
        this.achievementDescription = achievementDescription;
    }

    @Override
    public String toString() {
        return "EventParticipationRequestDTO{" +
                "eventName='" + eventName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", participationRole='" + participationRole + '\'' +
                ", achievementDescription='" + achievementDescription + '\'' +
                '}';
    }
}
