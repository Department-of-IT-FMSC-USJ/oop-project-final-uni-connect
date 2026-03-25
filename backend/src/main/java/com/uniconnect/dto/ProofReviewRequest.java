package com.uniconnect.dto;

import com.uniconnect.model.PointCategory;
import jakarta.validation.constraints.NotBlank;

public class ProofReviewRequest {

    @NotBlank
    private String action;

    private Integer points;

    private PointCategory category;

    private String note;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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
