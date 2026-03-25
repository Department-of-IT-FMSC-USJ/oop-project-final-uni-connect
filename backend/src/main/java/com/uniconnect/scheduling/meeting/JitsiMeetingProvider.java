package com.uniconnect.scheduling.meeting;

import com.uniconnect.scheduling.config.SchedulingProperties;
import com.uniconnect.scheduling.model.MeetingProviderType;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

@Component
public class JitsiMeetingProvider implements MeetingProvider {

    private final SchedulingProperties schedulingProperties;

    public JitsiMeetingProvider(SchedulingProperties schedulingProperties) {
        this.schedulingProperties = schedulingProperties;
    }

    @Override
    public MeetingProviderType providerType() {
        return MeetingProviderType.JITSI;
    }

    @Override
    public MeetingDetails createMeeting(MeetingRequest request) {
        String prefix = safeSegment(schedulingProperties.getMeeting().getJitsiRoomPrefix());
        String titleSegment = safeSegment(request.getTitle());
        String sessionSegment = request.getSessionId() == null ? "pending" : String.valueOf(request.getSessionId());
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toLowerCase(Locale.ROOT);

        String roomName = prefix + "-" + titleSegment + "-" + sessionSegment + "-" + random;
        String joinUrl = trimTrailingSlash(schedulingProperties.getMeeting().getJitsiBaseUrl()) + "/" + roomName;
        return new MeetingDetails(joinUrl, roomName);
    }

    private String safeSegment(String raw) {
        if (raw == null || raw.isBlank()) {
            return "session";
        }

        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        if (normalized.isBlank()) {
            return "session";
        }
        return normalized.length() > 24 ? normalized.substring(0, 24) : normalized;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "https://meet.jit.si";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}

