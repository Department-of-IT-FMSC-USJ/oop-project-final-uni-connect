package com.uniconnect.scheduling.dto;

import com.uniconnect.scheduling.model.SessionParticipantRole;

public class SessionParticipantResponse {
    private Long userId;
    private String fullName;
    private String email;
    private SessionParticipantRole participantRole;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public SessionParticipantRole getParticipantRole() {
        return participantRole;
    }

    public void setParticipantRole(SessionParticipantRole participantRole) {
        this.participantRole = participantRole;
    }
}

