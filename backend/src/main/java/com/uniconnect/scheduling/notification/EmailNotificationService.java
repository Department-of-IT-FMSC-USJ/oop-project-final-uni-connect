package com.uniconnect.scheduling.notification;

import java.util.List;

public interface EmailNotificationService {
    void notifyUsers(List<Long> userIds, String subject, String body);
}

