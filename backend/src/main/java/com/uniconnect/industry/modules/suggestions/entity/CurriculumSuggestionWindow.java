package com.uniconnect.industry.modules.suggestions.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "curriculum_suggestion_window")
public class CurriculumSuggestionWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submissions_open", nullable = false)
    private boolean submissionsOpen;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CurriculumSuggestionWindow() {
    }

    @PrePersist
    @PreUpdate
    protected void onWrite() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public boolean isSubmissionsOpen() {
        return submissionsOpen;
    }

    public void setSubmissionsOpen(boolean submissionsOpen) {
        this.submissionsOpen = submissionsOpen;
    }

    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(Long updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
