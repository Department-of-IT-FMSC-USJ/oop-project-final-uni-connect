package com.uniconnect.controller;

import com.uniconnect.model.SystemNotification;
import com.uniconnect.model.User;
import com.uniconnect.service.SystemNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class SystemNotificationController {

    private final SystemNotificationService systemNotificationService;

    public SystemNotificationController(SystemNotificationService systemNotificationService) {
        this.systemNotificationService = systemNotificationService;
    }

    @GetMapping
    public ResponseEntity<List<SystemNotification>> getNotifications(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(systemNotificationService.getRecentNotifications(user.getId()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of("unreadCount", systemNotificationService.getUnreadCount(user.getId())));
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Map<String, Boolean>> markAllRead(@AuthenticationPrincipal User user) {
        systemNotificationService.markAllRead(user.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }
}
