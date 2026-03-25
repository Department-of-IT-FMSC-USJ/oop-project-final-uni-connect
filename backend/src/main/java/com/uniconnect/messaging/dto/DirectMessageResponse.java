package com.uniconnect.messaging.dto;

import java.time.LocalDateTime;

public class DirectMessageResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long recipientId;
    private String recipientName;
    private String content;
    private LocalDateTime createdAt;

    public DirectMessageResponse(Long id, Long senderId, String senderName, Long recipientId,
                                 String recipientName, String content, LocalDateTime createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public Long getRecipientId() { return recipientId; }
    public String getRecipientName() { return recipientName; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
