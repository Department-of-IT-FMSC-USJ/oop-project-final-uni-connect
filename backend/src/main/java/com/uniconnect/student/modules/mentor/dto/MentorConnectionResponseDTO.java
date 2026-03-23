package com.uniconnect.student.modules.mentor.dto;

import com.uniconnect.student.modules.mentor.enums.ConnectionStatus;
import com.uniconnect.student.modules.mentor.enums.MentorType;

import java.time.LocalDateTime;

/**
 * DTO for mentor connection response.
 */
public class MentorConnectionResponseDTO {

    private Integer connectionId;
    private Integer studentId;
    private Integer mentorId;
    private MentorType mentorType;
    private ConnectionStatus connectionStatus;
    private LocalDateTime createdDate;

    public MentorConnectionResponseDTO() {
    }

    public MentorConnectionResponseDTO(Integer connectionId, Integer studentId, Integer mentorId,
                                        MentorType mentorType, ConnectionStatus connectionStatus,
                                        LocalDateTime createdDate) {
        this.connectionId = connectionId;
        this.studentId = studentId;
        this.mentorId = mentorId;
        this.mentorType = mentorType;
        this.connectionStatus = connectionStatus;
        this.createdDate = createdDate;
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
        return "MentorConnectionResponseDTO{" +
                "connectionId=" + connectionId +
                ", studentId=" + studentId +
                ", mentorId=" + mentorId +
                ", mentorType=" + mentorType +
                ", connectionStatus=" + connectionStatus +
                ", createdDate=" + createdDate +
                '}';
    }

    // Static Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer connectionId;
        private Integer studentId;
        private Integer mentorId;
        private MentorType mentorType;
        private ConnectionStatus connectionStatus;
        private LocalDateTime createdDate;

        public Builder connectionId(Integer connectionId) {
            this.connectionId = connectionId;
            return this;
        }

        public Builder studentId(Integer studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder mentorId(Integer mentorId) {
            this.mentorId = mentorId;
            return this;
        }

        public Builder mentorType(MentorType mentorType) {
            this.mentorType = mentorType;
            return this;
        }

        public Builder connectionStatus(ConnectionStatus connectionStatus) {
            this.connectionStatus = connectionStatus;
            return this;
        }

        public Builder createdDate(LocalDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public MentorConnectionResponseDTO build() {
            return new MentorConnectionResponseDTO(
                    connectionId, studentId, mentorId, mentorType,
                    connectionStatus, createdDate);
        }
    }
}
