package com.uniconnect.dto;

import com.uniconnect.model.PointStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProofSummary {
    private Long proofId;
    private String title;
    private LocalDate eventDate;
    private String proofType;
    private LocalDateTime createdAt;
    private Integer points;
    private PointStatus status;

    public ProofSummary(Long proofId, String title, LocalDate eventDate, String proofType,
                        LocalDateTime createdAt, Integer points, PointStatus status) {
        this.proofId = proofId;
        this.title = title;
        this.eventDate = eventDate;
        this.proofType = proofType;
        this.createdAt = createdAt;
        this.points = points;
        this.status = status;
    }

    public Long getProofId() {
        return proofId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public String getProofType() {
        return proofType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Integer getPoints() {
        return points;
    }

    public PointStatus getStatus() {
        return status;
    }
}
