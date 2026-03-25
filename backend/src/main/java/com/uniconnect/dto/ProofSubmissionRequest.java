package com.uniconnect.dto;

import com.uniconnect.model.PointCategory;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ProofSubmissionRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private LocalDate eventDate;

    private String proofData;

    @NotNull
    private PointCategory category;

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

    public PointCategory getCategory() {
        return category;
    }

    public void setCategory(PointCategory category) {
        this.category = category;
    }

    @JsonSetter("pointCategory")
    @JsonAlias({ "category", "pointCategory" })
    public void setCategoryFromString(String category) {
        if (category == null) {
            this.category = null;
            return;
        }
        this.category = PointCategory.valueOf(category.trim().toUpperCase());
    }
}
