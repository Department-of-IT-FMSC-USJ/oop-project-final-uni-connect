package com.uniconnect.repository;

import com.uniconnect.model.SystemNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {
    List<SystemNotification> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndReadFalse(Long userId);
    List<SystemNotification> findByUserIdAndReadFalse(Long userId);
}
