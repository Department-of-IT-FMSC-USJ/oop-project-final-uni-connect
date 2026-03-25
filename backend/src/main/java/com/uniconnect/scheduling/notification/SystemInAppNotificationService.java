package com.uniconnect.scheduling.notification;

import com.uniconnect.service.SystemNotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemInAppNotificationService implements InAppNotificationService {

    private final SystemNotificationService systemNotificationService;

    public SystemInAppNotificationService(SystemNotificationService systemNotificationService) {
        this.systemNotificationService = systemNotificationService;
    }

    @Override
    public void notifyUsers(List<Long> userIds, String title, String message, String link) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            if (userId == null) {
                continue;
            }
            systemNotificationService.createNotification(userId, title, message, link);
        }
    }
}

