package com.uniconnect.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_records")
public class PointRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proof_id")
    private ProofSubmission proofSubmission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "allocated_by", nullable = false)
    private User allocatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Enumerated(EnumType.STRING)
    private PointCategory category;

    @Enumerated(EnumType.STRING)
    private PointStatus status;

    private Integer originalPoints;
    private Integer points;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(columnDefinition = "TEXT")
    private String reviewNote;

    private LocalDateTime allocatedAt;
    private LocalDateTime reviewedAt;

    public PointRecord() {
    }

    public PointRecord(User student, ProofSubmission proofSubmission, User allocatedBy, PointCategory category,
                       PointStatus status, Integer originalPoints, Integer points, String note, LocalDateTime allocatedAt) {
        this.student = student;
        this.proofSubmission = proofSubmission;
        this.allocatedBy = allocatedBy;
        this.category = category;
        this.status = status;
        this.originalPoints = originalPoints;
        this.points = points;
        this.note = note;
        this.allocatedAt = allocatedAt;
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

    public ProofSubmission getProofSubmission() {
        return proofSubmission;
    }

    public void setProofSubmission(ProofSubmission proofSubmission) {
        this.proofSubmission = proofSubmission;
    }

    public User getAllocatedBy() {
        return allocatedBy;
    }

    public void setAllocatedBy(User allocatedBy) {
        this.allocatedBy = allocatedBy;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public PointCategory getCategory() {
        return category;
    }

    public void setCategory(PointCategory category) {
        this.category = category;
    }

    public PointStatus getStatus() {
        return status;
    }

    public void setStatus(PointStatus status) {
        this.status = status;
    }

    public Integer getOriginalPoints() {
        return originalPoints;
    }

    public void setOriginalPoints(Integer originalPoints) {
        this.originalPoints = originalPoints;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public LocalDateTime getAllocatedAt() {
        return allocatedAt;
    }

    public void setAllocatedAt(LocalDateTime allocatedAt) {
        this.allocatedAt = allocatedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
