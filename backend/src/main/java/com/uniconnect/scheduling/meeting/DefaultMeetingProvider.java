package com.uniconnect.scheduling.meeting;

import com.uniconnect.scheduling.config.SchedulingProperties;
import com.uniconnect.scheduling.model.MeetingProviderType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DefaultMeetingProvider implements MeetingProvider {

    private final SchedulingProperties schedulingProperties;

    public DefaultMeetingProvider(SchedulingProperties schedulingProperties) {
        this.schedulingProperties = schedulingProperties;
    }

    @Override
    public MeetingProviderType providerType() {
        return MeetingProviderType.INTERNAL_DEFAULT;
    }

    @Override
    public MeetingDetails createMeeting(MeetingRequest request) {
        String reference = (request.getSessionId() == null ? "pending" : request.getSessionId())
                + "-" + UUID.randomUUID().toString().substring(0, 8);
        String baseUrl = schedulingProperties.getMeeting().getBaseJoinUrl();
        String joinUrl = baseUrl + "/" + reference;
        return new MeetingDetails(joinUrl, reference);
    }
}

