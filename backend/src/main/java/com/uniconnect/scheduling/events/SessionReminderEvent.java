package com.uniconnect.scheduling.events;

import java.time.LocalDateTime;
import java.util.List;

public class SessionReminderEvent {
    private final Long sessionId;
    private final String title;
    private final String message;
    private final LocalDateTime startsAt;
    private final List<Long> participantUserIds;

    public SessionReminderEvent(Long sessionId, String title, String message, LocalDateTime startsAt, List<Long> participantUserIds) {
        this.sessionId = sessionId;
        this.title = title;
        this.message = message;
        this.startsAt = startsAt;
        this.participantUserIds = participantUserIds;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public List<Long> getParticipantUserIds() {
        return participantUserIds;
    }
}

