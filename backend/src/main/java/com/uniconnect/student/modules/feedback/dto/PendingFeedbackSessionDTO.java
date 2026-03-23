package com.uniconnect.student.modules.feedback.dto;

import java.time.LocalDateTime;

/**
 * DTO returned to a student for showing which completed sessions still need feedback.
 */
public class PendingFeedbackSessionDTO {

    private Integer sessionId;
    private Integer mentorId;
    private String mentorName;
    private LocalDateTime sessionDate;

    public PendingFeedbackSessionDTO() {
    }

    public PendingFeedbackSessionDTO(Integer sessionId, Integer mentorId, String mentorName, LocalDateTime sessionDate) {
        this.sessionId = sessionId;
        this.mentorId = mentorId;
        this.mentorName = mentorName;
        this.sessionDate = sessionDate;
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getMentorId() {
        return mentorId;
    }

    public void setMentorId(Integer mentorId) {
        this.mentorId = mentorId;
    }

    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }
}

