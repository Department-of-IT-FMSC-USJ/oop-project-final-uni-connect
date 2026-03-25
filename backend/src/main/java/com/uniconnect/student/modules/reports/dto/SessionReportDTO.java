package com.uniconnect.student.modules.reports.dto;

import java.util.List;

public class SessionReportDTO {

    private Integer sessionId;
    private String sessionTitle;
    private String sessionTopic;
    private String sessionType;
    private String mentorName;
    private String mentorType;
    private String sessionDate;
    private String sessionTime;
    private String audienceMode;
    private int participantCount;
    private List<StudentParticipantDTO> participants;

    public SessionReportDTO() {}

    public Integer getSessionId() { return sessionId; }
    public void setSessionId(Integer sessionId) { this.sessionId = sessionId; }

    public String getSessionTitle() { return sessionTitle; }
    public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }

    public String getSessionTopic() { return sessionTopic; }
    public void setSessionTopic(String sessionTopic) { this.sessionTopic = sessionTopic; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getMentorName() { return mentorName; }
    public void setMentorName(String mentorName) { this.mentorName = mentorName; }

    public String getMentorType() { return mentorType; }
    public void setMentorType(String mentorType) { this.mentorType = mentorType; }

    public String getSessionDate() { return sessionDate; }
    public void setSessionDate(String sessionDate) { this.sessionDate = sessionDate; }

    public String getSessionTime() { return sessionTime; }
    public void setSessionTime(String sessionTime) { this.sessionTime = sessionTime; }

    public String getAudienceMode() { return audienceMode; }
    public void setAudienceMode(String audienceMode) { this.audienceMode = audienceMode; }

    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }

    public List<StudentParticipantDTO> getParticipants() { return participants; }
    public void setParticipants(List<StudentParticipantDTO> participants) { this.participants = participants; }
}
