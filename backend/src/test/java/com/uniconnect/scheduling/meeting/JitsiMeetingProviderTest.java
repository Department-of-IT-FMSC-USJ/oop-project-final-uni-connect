package com.uniconnect.scheduling.meeting;

import com.uniconnect.scheduling.config.SchedulingProperties;
import com.uniconnect.scheduling.model.MeetingProviderType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class JitsiMeetingProviderTest {

    @Test
    void generatesMeetJitsiJoinUrlWithSafeRoomName() {
        SchedulingProperties properties = new SchedulingProperties();
        properties.getMeeting().setJitsiBaseUrl("https://meet.jit.si");
        properties.getMeeting().setJitsiRoomPrefix("uni connect");

        JitsiMeetingProvider provider = new JitsiMeetingProvider(properties);
        MeetingDetails details = provider.createMeeting(new MeetingRequest(
                MeetingProviderType.JITSI,
                "AI / ML Intro @ 2026",
                LocalDateTime.now().plusDays(1),
                60,
                88L
        ));

        Assertions.assertTrue(details.getJoinUrl().startsWith("https://meet.jit.si/"));
        Assertions.assertFalse(details.getReference().contains(" "));
        Assertions.assertFalse(details.getReference().contains("@"));
        Assertions.assertTrue(details.getReference().contains("88"));
    }
}

