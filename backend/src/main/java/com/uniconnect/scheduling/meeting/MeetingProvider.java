package com.uniconnect.scheduling.meeting;

import com.uniconnect.scheduling.model.MeetingProviderType;

public interface MeetingProvider {
    MeetingProviderType providerType();

    MeetingDetails createMeeting(MeetingRequest request);
}

