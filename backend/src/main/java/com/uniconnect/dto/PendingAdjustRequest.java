package com.uniconnect.dto;

public class PendingAdjustRequest {
    private Integer adjustedPoints;
    private String note;
    private String category;

    public Integer getAdjustedPoints() {
        return adjustedPoints;
    }

    public void setAdjustedPoints(Integer adjustedPoints) {
        this.adjustedPoints = adjustedPoints;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
