package com.uniconnect.dto;

import jakarta.validation.constraints.NotBlank;

public class ReviewPointsRequest {

    @NotBlank
    private String action;

    private Integer adjustedPoints;

    private String note;

    private String category;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getAdjustedPoints() {
        return adjustedPoints;
    }

    public void setAdjustedPoints(Integer adjustedPoints) {
        this.adjustedPoints = adjustedPoints;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
