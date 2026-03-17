package com.app.modules.events.dto;

import com.app.modules.events.enums.VerificationStatus;

import java.time.LocalDateTime;

/**
 * DTO for event participation response.
 */
public class EventParticipationResponseDTO {

    private Integer participationId;
    private Integer studentId;
    private String eventName;
    private String eventType;
    private String participationRole;
    private String achievementDescription;
    private String evidenceFile;
    private VerificationStatus verificationStatus;
    private LocalDateTime submittedDate;

    public EventParticipationResponseDTO() {
    }

    public EventParticipationResponseDTO(Integer participationId, Integer studentId,
                                          String eventName, String eventType,
                                          String participationRole, String achievementDescription,
                                          String evidenceFile, VerificationStatus verificationStatus,
                                          LocalDateTime submittedDate) {
        this.participationId = participationId;
        this.studentId = studentId;
        this.eventName = eventName;
        this.eventType = eventType;
        this.participationRole = participationRole;
        this.achievementDescription = achievementDescription;
        this.evidenceFile = evidenceFile;
        this.verificationStatus = verificationStatus;
        this.submittedDate = submittedDate;
    }

    // Getters and Setters

    public Integer getParticipationId() {
        return participationId;
    }

    public void setParticipationId(Integer participationId) {
        this.participationId = participationId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

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

    public String getEvidenceFile() {
        return evidenceFile;
    }

    public void setEvidenceFile(String evidenceFile) {
        this.evidenceFile = evidenceFile;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }

    @Override
    public String toString() {
        return "EventParticipationResponseDTO{" +
                "participationId=" + participationId +
                ", studentId=" + studentId +
                ", eventName='" + eventName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", participationRole='" + participationRole + '\'' +
                ", achievementDescription='" + achievementDescription + '\'' +
                ", evidenceFile='" + evidenceFile + '\'' +
                ", verificationStatus=" + verificationStatus +
                ", submittedDate=" + submittedDate +
                '}';
    }

    // Static Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer participationId;
        private Integer studentId;
        private String eventName;
        private String eventType;
        private String participationRole;
        private String achievementDescription;
        private String evidenceFile;
        private VerificationStatus verificationStatus;
        private LocalDateTime submittedDate;

        public Builder participationId(Integer participationId) {
            this.participationId = participationId;
            return this;
        }

        public Builder studentId(Integer studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder eventName(String eventName) {
            this.eventName = eventName;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder participationRole(String participationRole) {
            this.participationRole = participationRole;
            return this;
        }

        public Builder achievementDescription(String achievementDescription) {
            this.achievementDescription = achievementDescription;
            return this;
        }

        public Builder evidenceFile(String evidenceFile) {
            this.evidenceFile = evidenceFile;
            return this;
        }

        public Builder verificationStatus(VerificationStatus verificationStatus) {
            this.verificationStatus = verificationStatus;
            return this;
        }

        public Builder submittedDate(LocalDateTime submittedDate) {
            this.submittedDate = submittedDate;
            return this;
        }

        public EventParticipationResponseDTO build() {
            return new EventParticipationResponseDTO(
                    participationId, studentId, eventName, eventType,
                    participationRole, achievementDescription, evidenceFile,
                    verificationStatus, submittedDate);
        }
    }
}
