package com.uniconnect.scheduling.dto;

import com.uniconnect.scheduling.model.SessionStatus;
import jakarta.validation.constraints.NotNull;

public class SessionStatusUpdateRequest {

    @NotNull
    private SessionStatus status;

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }
}

