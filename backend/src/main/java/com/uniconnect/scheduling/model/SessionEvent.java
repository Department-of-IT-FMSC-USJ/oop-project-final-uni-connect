package com.uniconnect.scheduling.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "session_events")
public class SessionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "topic", length = 200)
    private String topic;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private SessionStatus status;

    @Column(name = "creator_user_id", nullable = false)
    private Long creatorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_provider", nullable = false, length = 64)
    private MeetingProviderType meetingProvider;

    @Column(name = "meeting_join_url", length = 600)
    private String meetingJoinUrl;

    @Column(name = "meeting_reference", length = 128)
    private String meetingReference;

    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SessionParticipant> participants = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addParticipant(SessionParticipant participant) {
        participant.setSession(this);
        this.participants.add(participant);
    }

    public void clearParticipants() {
        this.participants.clear();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public Long getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(Long creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public MeetingProviderType getMeetingProvider() {
        return meetingProvider;
    }

    public void setMeetingProvider(MeetingProviderType meetingProvider) {
        this.meetingProvider = meetingProvider;
    }

    public String getMeetingJoinUrl() {
        return meetingJoinUrl;
    }

    public void setMeetingJoinUrl(String meetingJoinUrl) {
        this.meetingJoinUrl = meetingJoinUrl;
    }

    public String getMeetingReference() {
        return meetingReference;
    }

    public void setMeetingReference(String meetingReference) {
        this.meetingReference = meetingReference;
    }

    public LocalDateTime getReminderSentAt() {
        return reminderSentAt;
    }

    public void setReminderSentAt(LocalDateTime reminderSentAt) {
        this.reminderSentAt = reminderSentAt;
    }

    @Transient
    public LocalDateTime getEndsAt() {
        if (startsAt == null || durationMinutes == null) {
            return null;
        }
        return startsAt.plusMinutes(durationMinutes);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<SessionParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<SessionParticipant> participants) {
        this.participants = participants;
    }
}

