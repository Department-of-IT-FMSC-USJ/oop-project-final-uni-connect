package com.uniconnect.service;

import com.uniconnect.model.SystemNotification;
import com.uniconnect.repository.SystemNotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SystemNotificationService {

    private final SystemNotificationRepository systemNotificationRepository;

    public SystemNotificationService(SystemNotificationRepository systemNotificationRepository) {
        this.systemNotificationRepository = systemNotificationRepository;
    }

    public void createNotification(Long userId, String title, String message, String link) {
        if (userId == null) {
            return;
        }
        systemNotificationRepository.save(
                new SystemNotification(userId, title, message, link, false, LocalDateTime.now())
        );
    }

    public List<SystemNotification> getRecentNotifications(Long userId) {
        return systemNotificationRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return systemNotificationRepository.countByUserIdAndReadFalse(userId);
    }

    public void markAllRead(Long userId) {
        List<SystemNotification> unread = systemNotificationRepository.findByUserIdAndReadFalse(userId);
        if (unread.isEmpty()) {
            return;
        }
        unread.forEach(item -> item.setRead(true));
        systemNotificationRepository.saveAll(unread);
    }
}
