package com.uniconnect.student.modules.feedback.entity;

import com.uniconnect.student.modules.feedback.enums.SessionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class mapped to the 'mentor_sessions' database table.
 * Used as a read reference to verify session completion before allowing feedback.
 */
@Entity
@Table(name = "mentor_sessions")
public class MentorSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Integer sessionId;

    @Column(name = "connection_id", nullable = false)
    private Integer connectionId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "mentor_id", nullable = false)
    private Integer mentorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false)
    private SessionStatus sessionStatus;

    @Column(name = "session_date", nullable = false)
    private LocalDateTime sessionDate;

    public MentorSession() {
    }

    // Getters and Setters

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(Integer connectionId) {
        this.connectionId = connectionId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getMentorId() {
        return mentorId;
    }

    public void setMentorId(Integer mentorId) {
        this.mentorId = mentorId;
    }

    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }

    @Override
    public String toString() {
        return "MentorSession{" +
                "sessionId=" + sessionId +
                ", connectionId=" + connectionId +
                ", studentId=" + studentId +
                ", mentorId=" + mentorId +
                ", sessionStatus=" + sessionStatus +
                ", sessionDate=" + sessionDate +
                '}';
    }
}
