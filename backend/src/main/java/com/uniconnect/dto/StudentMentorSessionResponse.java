package com.uniconnect.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class StudentMentorSessionResponse {
    private Integer sessionId;
    private Integer mentorId;
    private String mentorName;
    private String mentorRole;
    private String sessionTitle;
    private String sessionType;
    private String sessionTopic;
    private String sessionDescription;
    private LocalDate sessionDate;
    private LocalTime sessionTime;

    public StudentMentorSessionResponse(Integer sessionId, Integer mentorId, String mentorName, String mentorRole,
                                        String sessionTitle, String sessionType, String sessionTopic,
                                        String sessionDescription, LocalDate sessionDate, LocalTime sessionTime) {
        this.sessionId = sessionId;
        this.mentorId = mentorId;
        this.mentorName = mentorName;
        this.mentorRole = mentorRole;
        this.sessionTitle = sessionTitle;
        this.sessionType = sessionType;
        this.sessionTopic = sessionTopic;
        this.sessionDescription = sessionDescription;
        this.sessionDate = sessionDate;
        this.sessionTime = sessionTime;
    }

    public Integer getSessionId() { return sessionId; }
    public Integer getMentorId() { return mentorId; }
    public String getMentorName() { return mentorName; }
    public String getMentorRole() { return mentorRole; }
    public String getSessionTitle() { return sessionTitle; }
    public String getSessionType() { return sessionType; }
    public String getSessionTopic() { return sessionTopic; }
    public String getSessionDescription() { return sessionDescription; }
    public LocalDate getSessionDate() { return sessionDate; }
    public LocalTime getSessionTime() { return sessionTime; }
}
