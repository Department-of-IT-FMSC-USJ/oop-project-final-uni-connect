package com.app.modules.mentor.entity;

import com.app.modules.mentor.enums.ConnectionStatus;
import com.app.modules.mentor.enums.MentorType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class mapped to the 'mentor_connections' database table.
 */
@Entity
@Table(name = "mentor_connections")
public class MentorConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "connection_id")
    private Integer connectionId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "mentor_id", nullable = false)
    private Integer mentorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mentor_type", nullable = false)
    private MentorType mentorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", nullable = false)
    private ConnectionStatus connectionStatus;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    public MentorConnection() {
    }

    public MentorConnection(Integer connectionId, Integer studentId, Integer mentorId,
                             MentorType mentorType, ConnectionStatus connectionStatus,
                             LocalDateTime createdDate) {
        this.connectionId = connectionId;
        this.studentId = studentId;
        this.mentorId = mentorId;
        this.mentorType = mentorType;
        this.connectionStatus = connectionStatus;
        this.createdDate = createdDate;
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        if (this.connectionStatus == null) {
            this.connectionStatus = ConnectionStatus.Pending;
        }
    }

    // Getters and Setters

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

    public MentorType getMentorType() {
        return mentorType;
    }

    public void setMentorType(MentorType mentorType) {
        this.mentorType = mentorType;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "MentorConnection{" +
                "connectionId=" + connectionId +
                ", studentId=" + studentId +
                ", mentorId=" + mentorId +
                ", mentorType=" + mentorType +
                ", connectionStatus=" + connectionStatus +
                ", createdDate=" + createdDate +
                '}';
    }
}
