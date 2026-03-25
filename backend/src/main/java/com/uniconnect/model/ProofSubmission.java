package com.uniconnect.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "proof_submissions")
public class ProofSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String cpm;

    @Column(unique = true)
    private String submissionCode;

    private LocalDate eventDate;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String proofData;

    private String proofType;

    private LocalDateTime createdAt;

    private Integer latestPoints;

    @Enumerated(EnumType.STRING)
    private PointStatus latestStatus;

    @Enumerated(EnumType.STRING)
    private PointCategory latestCategory;

    public ProofSubmission() {
    }

    public ProofSubmission(User student, String title, String description, LocalDate eventDate,
                           String proofData, String proofType, LocalDateTime createdAt) {
        this.student = student;
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.proofData = proofData;
        this.proofType = proofType;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCpm() {
        return cpm;
    }

    public void setCpm(String cpm) {
        this.cpm = cpm;
    }

    public String getSubmissionCode() {
        return submissionCode;
    }

    public void setSubmissionCode(String submissionCode) {
        this.submissionCode = submissionCode;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getProofData() {
        return proofData;
    }

    public void setProofData(String proofData) {
        this.proofData = proofData;
    }

    public String getProofType() {
        return proofType;
    }

    public void setProofType(String proofType) {
        this.proofType = proofType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getLatestPoints() {
        return latestPoints;
    }

    public void setLatestPoints(Integer latestPoints) {
        this.latestPoints = latestPoints;
    }

    public PointStatus getLatestStatus() {
        return latestStatus;
    }

    public void setLatestStatus(PointStatus latestStatus) {
        this.latestStatus = latestStatus;
    }

    public PointCategory getLatestCategory() {
        return latestCategory;
    }

    public void setLatestCategory(PointCategory latestCategory) {
        this.latestCategory = latestCategory;
    }
}
