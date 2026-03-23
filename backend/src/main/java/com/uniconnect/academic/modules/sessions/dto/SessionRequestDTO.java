package com.uniconnect.academic.modules.sessions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO for mentoring session creation request.
 */
public class SessionRequestDTO {

    @NotNull(message = "Mentor ID is required")
    private Integer mentorId;

    @NotBlank(message = "Session title is required")
    @Size(min = 3, max = 100, message = "Session title must be between 3 and 100 characters")
    private String sessionTitle;

    @NotBlank(message = "Session type is required")
    private String sessionType;

    @Size(max = 200, message = "Session topic cannot exceed 200 characters")
    private String sessionTopic;

    @Size(max = 500, message = "Session description cannot exceed 500 characters")
    private String sessionDescription;

    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;

    @NotNull(message = "Session time is required")
    private LocalTime sessionTime;

    private String audienceMode;

    private String targetYearOfStudy;

    private List<Integer> targetStudentIds;

    public SessionRequestDTO() {
    }

    public SessionRequestDTO(Integer mentorId, String sessionTitle, String sessionType,
                              String sessionTopic, String sessionDescription,
                              LocalDate sessionDate, LocalTime sessionTime) {
        this.mentorId = mentorId;
        this.sessionTitle = sessionTitle;
        this.sessionType = sessionType;
        this.sessionTopic = sessionTopic;
        this.sessionDescription = sessionDescription;
        this.sessionDate = sessionDate;
        this.sessionTime = sessionTime;
    }

    // Getters and Setters

    public Integer getMentorId() {
        return mentorId;
    }

    public void setMentorId(Integer mentorId) {
        this.mentorId = mentorId;
    }

    public String getSessionTitle() {
        return sessionTitle;
    }

    public void setSessionTitle(String sessionTitle) {
        this.sessionTitle = sessionTitle;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getSessionTopic() {
        return sessionTopic;
    }

    public void setSessionTopic(String sessionTopic) {
        this.sessionTopic = sessionTopic;
    }

    public String getSessionDescription() {
        return sessionDescription;
    }

    public void setSessionDescription(String sessionDescription) {
        this.sessionDescription = sessionDescription;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public LocalTime getSessionTime() {
        return sessionTime;
    }

    public void setSessionTime(LocalTime sessionTime) {
        this.sessionTime = sessionTime;
    }

    public String getAudienceMode() {
        return audienceMode;
    }

    public void setAudienceMode(String audienceMode) {
        this.audienceMode = audienceMode;
    }

    public String getTargetYearOfStudy() {
        return targetYearOfStudy;
    }

    public void setTargetYearOfStudy(String targetYearOfStudy) {
        this.targetYearOfStudy = targetYearOfStudy;
    }

    public List<Integer> getTargetStudentIds() {
        return targetStudentIds;
    }

    public void setTargetStudentIds(List<Integer> targetStudentIds) {
        this.targetStudentIds = targetStudentIds;
    }

    @Override
    public String toString() {
        return "SessionRequestDTO{" +
                "mentorId=" + mentorId +
                ", sessionTitle='" + sessionTitle + '\'' +
                ", sessionType='" + sessionType + '\'' +
                ", sessionTopic='" + sessionTopic + '\'' +
                ", sessionDescription='" + sessionDescription + '\'' +
                ", sessionDate=" + sessionDate +
                ", sessionTime=" + sessionTime +
                ", audienceMode='" + audienceMode + '\'' +
                ", targetYearOfStudy='" + targetYearOfStudy + '\'' +
                ", targetStudentIds=" + targetStudentIds +
                '}';
    }
}
