package com.uniconnect.scheduling.meeting;

import com.uniconnect.scheduling.config.SchedulingProperties;
import com.uniconnect.scheduling.model.MeetingProviderType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class DefaultMeetingProviderTest {

    @Test
    void createsJoinUrlWithConfiguredBasePath() {
        SchedulingProperties properties = new SchedulingProperties();
        properties.getMeeting().setBaseJoinUrl("http://localhost:8080/meet");

        DefaultMeetingProvider provider = new DefaultMeetingProvider(properties);
        MeetingDetails details = provider.createMeeting(new MeetingRequest(
                MeetingProviderType.INTERNAL_DEFAULT,
                "Design Discussion",
                LocalDateTime.now().plusDays(1),
                60,
                42L
        ));

        Assertions.assertNotNull(details.getReference());
        Assertions.assertTrue(details.getJoinUrl().startsWith("http://localhost:8080/meet/42-"));
    }
}

