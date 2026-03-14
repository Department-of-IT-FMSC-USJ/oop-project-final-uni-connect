package com.uniconnect.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProofSubmissionResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String title;
    private String description;
    private String cpm;
    private String submissionCode;
    private LocalDate eventDate;
    private String proofData;
    private String proofType;
    private LocalDateTime createdAt;
    private Integer latestPoints;
    private String pointStatus;
    private String pointCategory;

    public ProofSubmissionResponse(Long id, Long studentId, String studentName, String title, String description,
                                   String cpm, String submissionCode, LocalDate eventDate, String proofData,
                                   String proofType, LocalDateTime createdAt, Integer latestPoints, String pointStatus,
                                   String pointCategory) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.title = title;
        this.description = description;
        this.cpm = cpm;
        this.submissionCode = submissionCode;
        this.eventDate = eventDate;
        this.proofData = proofData;
        this.proofType = proofType;
        this.createdAt = createdAt;
        this.latestPoints = latestPoints;
        this.pointStatus = pointStatus;
        this.pointCategory = pointCategory;
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

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCpm() {
        return cpm;
    }

    public String getSubmissionCode() {
        return submissionCode;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public String getProofData() {
        return proofData;
    }

    public String getProofType() {
        return proofType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Integer getLatestPoints() {
        return latestPoints;
    }

    public String getPointStatus() {
        return pointStatus;
    }

    public String getPointCategory() {
        return pointCategory;
    }
}
