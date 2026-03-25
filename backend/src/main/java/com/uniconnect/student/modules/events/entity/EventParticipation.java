package com.uniconnect.student.modules.events.entity;

import com.uniconnect.student.modules.events.enums.VerificationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class mapped to the 'event_participations' database table.
 */
@Entity
@Table(name = "event_participations")
public class EventParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participation_id")
    private Integer participationId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "event_name", nullable = false, length = 150)
    private String eventName;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "participation_role", nullable = false, length = 50)
    private String participationRole;

    @Column(name = "achievement_description", columnDefinition = "TEXT")
    private String achievementDescription;

    @Column(name = "evidence_file", length = 255)
    private String evidenceFile;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus;

    @Column(name = "submitted_date", nullable = false)
    private LocalDateTime submittedDate;

    public EventParticipation() {
    }

    public EventParticipation(Integer participationId, Integer studentId, String eventName,
                               String eventType, String participationRole,
                               String achievementDescription, String evidenceFile,
                               VerificationStatus verificationStatus, LocalDateTime submittedDate) {
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

    @PrePersist
    protected void onCreate() {
        this.submittedDate = LocalDateTime.now();
        if (this.verificationStatus == null) {
            this.verificationStatus = VerificationStatus.Pending;
        }
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
        return "EventParticipation{" +
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
}
