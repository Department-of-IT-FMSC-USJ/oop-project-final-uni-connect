package com.uniconnect.dto;

import com.uniconnect.model.PointCategory;
import jakarta.validation.constraints.NotNull;

public class AllocatePointsRequest {

    @NotNull
    private Long studentId;

    private Long proofId;

    @NotNull
    private Integer points;

    @NotNull
    private PointCategory category;

    private String note;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getProofId() {
        return proofId;
    }

    public void setProofId(Long proofId) {
        this.proofId = proofId;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public PointCategory getCategory() {
        return category;
    }

    public void setCategory(PointCategory category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
