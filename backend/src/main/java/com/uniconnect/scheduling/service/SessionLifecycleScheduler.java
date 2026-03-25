package com.uniconnect.scheduling.service;

import com.uniconnect.scheduling.config.SchedulingProperties;
import com.uniconnect.scheduling.events.SessionReminderEvent;
import com.uniconnect.scheduling.model.SessionEvent;
import com.uniconnect.scheduling.model.SessionStatus;
import com.uniconnect.scheduling.repository.SessionEventRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
public class SessionLifecycleScheduler {

    private static final Set<SessionStatus> MISSED_CANDIDATES = Set.of(SessionStatus.CREATED, SessionStatus.SCHEDULED);

    private final SessionEventRepository sessionEventRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SchedulingProperties schedulingProperties;

    public SessionLifecycleScheduler(
            SessionEventRepository sessionEventRepository,
            ApplicationEventPublisher eventPublisher,
            SchedulingProperties schedulingProperties
    ) {
        this.sessionEventRepository = sessionEventRepository;
        this.eventPublisher = eventPublisher;
        this.schedulingProperties = schedulingProperties;
    }

    @Scheduled(fixedDelayString = "${uniconnect.scheduling.lifecycle.fixed-delay-ms:60000}")
    @Transactional
    public void autoMarkMissedSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<SessionEvent> candidates = sessionEventRepository.findByStatusInAndStartsAtBefore(MISSED_CANDIDATES, now);
        for (SessionEvent session : candidates) {
            LocalDateTime endsAt = session.getEndsAt();
            if (endsAt != null && endsAt.isBefore(now)) {
                session.setStatus(SessionStatus.MISSED);
            }
        }
    }

    @Scheduled(fixedDelayString = "${uniconnect.scheduling.reminders.fixed-delay-ms:60000}")
    @Transactional
    public void sendUpcomingReminders() {
        LocalDateTime now = LocalDateTime.now();
        int lead = Math.max(1, schedulingProperties.getMeeting().getReminderLeadMinutes());
        LocalDateTime threshold = now.plusMinutes(lead);

        List<SessionEvent> sessions = sessionEventRepository.findPendingReminders(SessionStatus.SCHEDULED, now, threshold);
        for (SessionEvent session : sessions) {
            java.util.LinkedHashSet<Long> participantIds = new java.util.LinkedHashSet<>();
            participantIds.add(session.getCreatorUserId());
            for (var participant : session.getParticipants()) {
                participantIds.add(participant.getUserId());
            }
            eventPublisher.publishEvent(new SessionReminderEvent(
                    session.getId(),
                    "Session Reminder",
                    "Reminder: '" + session.getTitle() + "' starts at " + session.getStartsAt() + ".",
                    session.getStartsAt(),
                    participantIds.stream().filter(java.util.Objects::nonNull).toList()
            ));
            session.setReminderSentAt(now);
        }
    }
}


