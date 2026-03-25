package com.uniconnect.scheduling.dto;

import com.uniconnect.scheduling.model.MeetingProviderType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class SessionCreateRequest {

    @NotBlank
    @Size(max = 150)
    private String title;

    @Size(max = 200)
    private String topic;

    @Size(max = 2000)
    private String description;

    @NotNull
    @Future
    private LocalDateTime startsAt;

    @NotNull
    @Min(15)
    @Max(480)
    private Integer durationMinutes;

    private MeetingProviderType meetingProvider;

    @NotNull
    @Size(min = 1)
    private List<Long> participantUserIds;

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

    public MeetingProviderType getMeetingProvider() {
        return meetingProvider;
    }

    public void setMeetingProvider(MeetingProviderType meetingProvider) {
        this.meetingProvider = meetingProvider;
    }

    public List<Long> getParticipantUserIds() {
        return participantUserIds;
    }

    public void setParticipantUserIds(List<Long> participantUserIds) {
        this.participantUserIds = participantUserIds;
    }
}

