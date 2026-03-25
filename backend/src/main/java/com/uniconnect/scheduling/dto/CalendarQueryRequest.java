package com.uniconnect.scheduling.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class CalendarQueryRequest {

    @NotNull
    private LocalDateTime from;

    @NotNull
    private LocalDateTime to;

    private Long userId;

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public void setTo(LocalDateTime to) {
        this.to = to;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

