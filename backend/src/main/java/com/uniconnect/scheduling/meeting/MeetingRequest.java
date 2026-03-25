package com.uniconnect.scheduling.meeting;

import com.uniconnect.scheduling.model.MeetingProviderType;

import java.time.LocalDateTime;

public class MeetingRequest {
    private final MeetingProviderType providerType;
    private final String title;
    private final LocalDateTime startsAt;
    private final Integer durationMinutes;
    private final Long sessionId;

    public MeetingRequest(MeetingProviderType providerType, String title, LocalDateTime startsAt, Integer durationMinutes, Long sessionId) {
        this.providerType = providerType;
        this.title = title;
        this.startsAt = startsAt;
        this.durationMinutes = durationMinutes;
        this.sessionId = sessionId;
    }

    public MeetingProviderType getProviderType() {
        return providerType;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public Long getSessionId() {
        return sessionId;
    }
}

