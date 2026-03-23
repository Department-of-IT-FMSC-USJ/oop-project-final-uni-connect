package com.uniconnect.messaging.dto;

import com.uniconnect.model.Role;

import java.time.LocalDateTime;

public class ConversationSummaryResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String department;
    private Role role;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;

    public ConversationSummaryResponse(Long userId, String fullName, String email, String department,
                                       Role role, String lastMessage, LocalDateTime lastMessageAt, long unreadCount) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.role = role;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;
    }

    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public Role getRole() { return role; }
    public String getLastMessage() { return lastMessage; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public long getUnreadCount() { return unreadCount; }
}
