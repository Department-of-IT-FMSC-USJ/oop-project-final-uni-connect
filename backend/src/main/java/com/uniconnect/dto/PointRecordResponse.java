package com.uniconnect.dto;

import com.uniconnect.model.PointCategory;
import com.uniconnect.model.PointStatus;

import java.time.LocalDateTime;

public class PointRecordResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long proofId;
    private Integer originalPoints;
    private Integer points;
    private PointCategory category;
    private PointStatus status;
    private String note;
    private String reviewNote;
    private String allocatedByName;
    private String reviewedByName;
    private LocalDateTime allocatedAt;
    private LocalDateTime reviewedAt;

    public PointRecordResponse(Long id, Long studentId, String studentName, Long proofId, Integer originalPoints,
                               Integer points, PointCategory category, PointStatus status, String note,
                               String reviewNote, String allocatedByName, String reviewedByName,
                               LocalDateTime allocatedAt, LocalDateTime reviewedAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.proofId = proofId;
        this.originalPoints = originalPoints;
        this.points = points;
        this.category = category;
        this.status = status;
        this.note = note;
        this.reviewNote = reviewNote;
        this.allocatedByName = allocatedByName;
        this.reviewedByName = reviewedByName;
        this.allocatedAt = allocatedAt;
        this.reviewedAt = reviewedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public Long getProofId() {
        return proofId;
    }

    public Integer getOriginalPoints() {
        return originalPoints;
    }

    public Integer getPoints() {
        return points;
    }

    public PointCategory getCategory() {
        return category;
    }

    public PointStatus getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public String getAllocatedByName() {
        return allocatedByName;
    }

    public String getReviewedByName() {
        return reviewedByName;
    }

    public LocalDateTime getAllocatedAt() {
        return allocatedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
}
