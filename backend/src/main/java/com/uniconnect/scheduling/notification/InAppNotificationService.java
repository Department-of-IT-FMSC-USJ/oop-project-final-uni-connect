package com.uniconnect.scheduling.notification;

import java.util.List;

public interface InAppNotificationService {
    void notifyUsers(List<Long> userIds, String title, String message, String link);
}

