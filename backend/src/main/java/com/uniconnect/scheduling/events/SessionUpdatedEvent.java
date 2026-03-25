package com.uniconnect.scheduling.events;

import java.util.List;

public class SessionUpdatedEvent {
    private final Long sessionId;
    private final String title;
    private final String message;
    private final List<Long> participantUserIds;

    public SessionUpdatedEvent(Long sessionId, String title, String message, List<Long> participantUserIds) {
        this.sessionId = sessionId;
        this.title = title;
        this.message = message;
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

    public List<Long> getParticipantUserIds() {
        return participantUserIds;
    }
}


