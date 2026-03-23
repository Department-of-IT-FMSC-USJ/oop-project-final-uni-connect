package com.uniconnect.academic.modules.sessions.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO for mentoring session response.
 */
public class SessionResponseDTO {

    private Integer sessionId;
    private Integer mentorId;
    private String sessionTitle;
    private String sessionType;
    private String sessionTopic;
    private String sessionDescription;
    private LocalDate sessionDate;
    private LocalTime sessionTime;
    private LocalDateTime createdAt;

    public SessionResponseDTO() {
    }

    public SessionResponseDTO(Integer sessionId, Integer mentorId, String sessionTitle,
                                String sessionType, String sessionTopic, String sessionDescription,
                                LocalDate sessionDate, LocalTime sessionTime, LocalDateTime createdAt) {
        this.sessionId = sessionId;
        this.mentorId = mentorId;
        this.sessionTitle = sessionTitle;
        this.sessionType = sessionType;
        this.sessionTopic = sessionTopic;
        this.sessionDescription = sessionDescription;
        this.sessionDate = sessionDate;
        this.sessionTime = sessionTime;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public Integer getSessionId() { return sessionId; }
    public void setSessionId(Integer sessionId) { this.sessionId = sessionId; }
    public Integer getMentorId() { return mentorId; }
    public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }
    public String getSessionTitle() { return sessionTitle; }
    public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }
    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public String getSessionTopic() { return sessionTopic; }
    public void setSessionTopic(String sessionTopic) { this.sessionTopic = sessionTopic; }
    public String getSessionDescription() { return sessionDescription; }
    public void setSessionDescription(String sessionDescription) { this.sessionDescription = sessionDescription; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
    public LocalTime getSessionTime() { return sessionTime; }
    public void setSessionTime(LocalTime sessionTime) { this.sessionTime = sessionTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "SessionResponseDTO{" +
                "sessionId=" + sessionId +
                ", mentorId=" + mentorId +
                ", sessionTitle='" + sessionTitle + '\'' +
                ", sessionType='" + sessionType + '\'' +
                ", sessionTopic='" + sessionTopic + '\'' +
                ", sessionDescription='" + sessionDescription + '\'' +
                ", sessionDate=" + sessionDate +
                ", sessionTime=" + sessionTime +
                ", createdAt=" + createdAt +
                '}';
    }

    // Static Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer sessionId;
        private Integer mentorId;
        private String sessionTitle;
        private String sessionType;
        private String sessionTopic;
        private String sessionDescription;
        private LocalDate sessionDate;
        private LocalTime sessionTime;
        private LocalDateTime createdAt;

        public Builder sessionId(Integer sessionId) { this.sessionId = sessionId; return this; }
        public Builder mentorId(Integer mentorId) { this.mentorId = mentorId; return this; }
        public Builder sessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; return this; }
        public Builder sessionType(String sessionType) { this.sessionType = sessionType; return this; }
        public Builder sessionTopic(String sessionTopic) { this.sessionTopic = sessionTopic; return this; }
        public Builder sessionDescription(String sessionDescription) { this.sessionDescription = sessionDescription; return this; }
        public Builder sessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; return this; }
        public Builder sessionTime(LocalTime sessionTime) { this.sessionTime = sessionTime; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public SessionResponseDTO build() {
            return new SessionResponseDTO(sessionId, mentorId, sessionTitle, sessionType,
                    sessionTopic, sessionDescription, sessionDate, sessionTime, createdAt);
        }
    }
}
