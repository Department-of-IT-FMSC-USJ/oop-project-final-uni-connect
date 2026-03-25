package com.uniconnect.scheduling.notification;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoOpEmailNotificationService implements EmailNotificationService {

    @Override
    public void notifyUsers(List<Long> userIds, String subject, String body) {
        // Intentionally no-op for now. This hook is used to plug in SMTP or provider adapters later.
    }
}

