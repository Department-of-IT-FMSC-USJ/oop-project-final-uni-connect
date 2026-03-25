package com.uniconnect.scheduling.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "session_participants",
        uniqueConstraints = @UniqueConstraint(name = "uk_session_participant_user", columnNames = {"session_id", "user_id"})
)
public class SessionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionEvent session;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_role", nullable = false, length = 32)
    private SessionParticipantRole participantRole;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SessionEvent getSession() {
        return session;
    }

    public void setSession(SessionEvent session) {
        this.session = session;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public SessionParticipantRole getParticipantRole() {
        return participantRole;
    }

    public void setParticipantRole(SessionParticipantRole participantRole) {
        this.participantRole = participantRole;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

