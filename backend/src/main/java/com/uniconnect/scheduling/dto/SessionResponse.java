package com.uniconnect.scheduling.dto;

import com.uniconnect.scheduling.model.MeetingProviderType;
import com.uniconnect.scheduling.model.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;

public class SessionResponse {
    private Long id;
    private String title;
    private String topic;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Integer durationMinutes;
    private SessionStatus status;
    private Long creatorUserId;
    private MeetingProviderType meetingProvider;
    private String joinUrl;
    private String meetingJoinUrl;
    private String meetingReference;
    private Boolean canJoin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SessionParticipantResponse> participants;

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

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
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

    public String getJoinUrl() {
        return joinUrl;
    }

    public void setJoinUrl(String joinUrl) {
        this.joinUrl = joinUrl;
    }

    public Boolean getCanJoin() {
        return canJoin;
    }

    public void setCanJoin(Boolean canJoin) {
        this.canJoin = canJoin;
    }

    public String getMeetingReference() {
        return meetingReference;
    }

    public void setMeetingReference(String meetingReference) {
        this.meetingReference = meetingReference;
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

    public List<SessionParticipantResponse> getParticipants() {
        return participants;
    }

    public void setParticipants(List<SessionParticipantResponse> participants) {
        this.participants = participants;
    }
}

