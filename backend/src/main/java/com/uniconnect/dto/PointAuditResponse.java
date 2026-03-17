package com.uniconnect.dto;

import com.uniconnect.model.PointAction;

import java.time.LocalDateTime;

public class PointAuditResponse {
    private Long id;
    private Long pointRecordId;
    private PointAction action;
    private String actorName;
    private Integer beforePoints;
    private Integer afterPoints;
    private String note;
    private LocalDateTime createdAt;

    public PointAuditResponse(Long id, Long pointRecordId, PointAction action, String actorName,
                              Integer beforePoints, Integer afterPoints, String note, LocalDateTime createdAt) {
        this.id = id;
        this.pointRecordId = pointRecordId;
        this.action = action;
        this.actorName = actorName;
        this.beforePoints = beforePoints;
        this.afterPoints = afterPoints;
        this.note = note;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getPointRecordId() {
        return pointRecordId;
    }

    public PointAction getAction() {
        return action;
    }

    public String getActorName() {
        return actorName;
    }

    public Integer getBeforePoints() {
        return beforePoints;
    }

    public Integer getAfterPoints() {
        return afterPoints;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
