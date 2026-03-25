package com.uniconnect.scheduling.service;

import com.uniconnect.scheduling.events.SessionCancelledEvent;
import com.uniconnect.scheduling.events.SessionCreatedEvent;
import com.uniconnect.scheduling.events.SessionReminderEvent;
import com.uniconnect.scheduling.events.SessionUpdatedEvent;
import com.uniconnect.scheduling.notification.EmailNotificationService;
import com.uniconnect.scheduling.notification.InAppNotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SessionNotificationEventListener {

    private static final String SCHEDULING_LINK = "/calendar";

    private final InAppNotificationService inAppNotificationService;
    private final EmailNotificationService emailNotificationService;

    public SessionNotificationEventListener(
            InAppNotificationService inAppNotificationService,
            EmailNotificationService emailNotificationService
    ) {
        this.inAppNotificationService = inAppNotificationService;
        this.emailNotificationService = emailNotificationService;
    }

    @EventListener
    public void onSessionCreated(SessionCreatedEvent event) {
        dispatch(event.getParticipantUserIds(), event.getTitle(), event.getMessage());
    }

    @EventListener
    public void onSessionUpdated(SessionUpdatedEvent event) {
        dispatch(event.getParticipantUserIds(), event.getTitle(), event.getMessage());
    }

    @EventListener
    public void onSessionCancelled(SessionCancelledEvent event) {
        dispatch(event.getParticipantUserIds(), event.getTitle(), event.getMessage());
    }

    @EventListener
    public void onSessionReminder(SessionReminderEvent event) {
        dispatch(event.getParticipantUserIds(), event.getTitle(), event.getMessage());
    }

    private void dispatch(java.util.List<Long> recipients, String title, String message) {
        if (recipients == null || recipients.isEmpty()) {
            return;
        }
        inAppNotificationService.notifyUsers(recipients, title, message, SCHEDULING_LINK);
        emailNotificationService.notifyUsers(recipients, title, message);
    }
}

