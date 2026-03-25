package com.uniconnect.scheduling.meeting;

public class MeetingDetails {
    private final String joinUrl;
    private final String reference;

    public MeetingDetails(String joinUrl, String reference) {
        this.joinUrl = joinUrl;
        this.reference = reference;
    }

    public String getJoinUrl() {
        return joinUrl;
    }

    public String getReference() {
        return reference;
    }
}

