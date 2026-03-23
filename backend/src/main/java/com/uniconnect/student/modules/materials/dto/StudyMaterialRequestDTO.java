package com.uniconnect.student.modules.materials.dto;

import com.uniconnect.student.modules.materials.enums.MaterialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for study material upload request.
 */
public class StudyMaterialRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    @Pattern(regexp = ".*[a-zA-Z].*", message = "Title cannot contain only numbers or special characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Material type is required")
    private MaterialType materialType;

    @NotBlank(message = "Target year is required")
    private String targetYearOfStudy;

    public StudyMaterialRequestDTO() {
    }

    public StudyMaterialRequestDTO(String title, String description, MaterialType materialType) {
        this.title = title;
        this.description = description;
        this.materialType = materialType;
    }

    // Getters and Setters

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

    public MaterialType getMaterialType() {
        return materialType;
    }

    public void setMaterialType(MaterialType materialType) {
        this.materialType = materialType;
    }

    public String getTargetYearOfStudy() {
        return targetYearOfStudy;
    }

    public void setTargetYearOfStudy(String targetYearOfStudy) {
        this.targetYearOfStudy = targetYearOfStudy;
    }

    @Override
    public String toString() {
        return "StudyMaterialRequestDTO{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", materialType=" + materialType +
                ", targetYearOfStudy='" + targetYearOfStudy + '\'' +
                '}';
    }
}
