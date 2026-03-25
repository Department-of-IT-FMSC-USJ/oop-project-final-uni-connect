package com.uniconnect.academic.modules.sessions.entity;

import com.uniconnect.academic.modules.sessions.enums.SessionType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity class mapped to the 'mentoring_sessions' database table.
 */
@Entity(name = "AcademicMentoringSession")
@Table(name = "academic_mentoring_sessions")
public class MentoringSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Integer sessionId;

    @Column(name = "mentor_id", nullable = false)
    private Integer mentorId;

    @Column(name = "session_title", nullable = false, length = 100)
    private String sessionTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Column(name = "session_topic", nullable = false, length = 200)
    private String sessionTopic;

    @Column(name = "session_description", columnDefinition = "TEXT")
    private String sessionDescription;

    @Column(name = "audience_mode", length = 30)
    private String audienceMode;

    @Column(name = "target_year_of_study", length = 20)
    private String targetYearOfStudy;

    @Column(name = "target_student_ids", columnDefinition = "TEXT")
    private String targetStudentIds;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "session_time", nullable = false)
    private LocalTime sessionTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public MentoringSession() {
    }

    public MentoringSession(Integer sessionId, Integer mentorId, String sessionTitle,
                             SessionType sessionType, String sessionTopic, String sessionDescription,
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

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

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

    public String getSessionTitle() {
        return sessionTitle;
    }

    public void setSessionTitle(String sessionTitle) {
        this.sessionTitle = sessionTitle;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
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

    public String getTargetStudentIds() {
        return targetStudentIds;
    }

    public void setTargetStudentIds(String targetStudentIds) {
        this.targetStudentIds = targetStudentIds;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "MentoringSession{" +
                "sessionId=" + sessionId +
                ", mentorId=" + mentorId +
                ", sessionTitle='" + sessionTitle + '\'' +
                ", sessionType=" + sessionType +
                ", sessionTopic='" + sessionTopic + '\'' +
                ", sessionDescription='" + sessionDescription + '\'' +
                ", audienceMode='" + audienceMode + '\'' +
                ", targetYearOfStudy='" + targetYearOfStudy + '\'' +
                ", targetStudentIds='" + targetStudentIds + '\'' +
                ", sessionDate=" + sessionDate +
                ", sessionTime=" + sessionTime +
                ", createdAt=" + createdAt +
                '}';
    }
}
