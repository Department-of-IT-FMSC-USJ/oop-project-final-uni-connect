package com.uniconnect.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_audit")
public class PointAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_record_id", nullable = false)
    private PointRecord pointRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    private PointAction action;

    private Integer beforePoints;
    private Integer afterPoints;

    @Column(columnDefinition = "TEXT")
    private String note;

    private LocalDateTime createdAt;

    public PointAudit() {
    }

    public PointAudit(PointRecord pointRecord, User actor, PointAction action, Integer beforePoints,
                      Integer afterPoints, String note, LocalDateTime createdAt) {
        this.pointRecord = pointRecord;
        this.actor = actor;
        this.action = action;
        this.beforePoints = beforePoints;
        this.afterPoints = afterPoints;
        this.note = note;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public PointRecord getPointRecord() {
        return pointRecord;
    }

    public void setPointRecord(PointRecord pointRecord) {
        this.pointRecord = pointRecord;
    }

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }

    public PointAction getAction() {
        return action;
    }

    public void setAction(PointAction action) {
        this.action = action;
    }

    public Integer getBeforePoints() {
        return beforePoints;
    }

    public void setBeforePoints(Integer beforePoints) {
        this.beforePoints = beforePoints;
    }

    public Integer getAfterPoints() {
        return afterPoints;
    }

    public void setAfterPoints(Integer afterPoints) {
        this.afterPoints = afterPoints;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
