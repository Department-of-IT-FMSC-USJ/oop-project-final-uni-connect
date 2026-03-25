package com.uniconnect.student.modules.materials.dto;

import com.uniconnect.student.modules.materials.enums.MaterialType;

import java.time.LocalDateTime;

/**
 * DTO for study material response.
 */
public class StudyMaterialResponseDTO {

    private Integer materialId;
    private String title;
    private String description;
    private MaterialType materialType;
    private String fileUrl;
    private Integer uploadedBy;
    private String targetYearOfStudy;
    private LocalDateTime uploadDate;

    public StudyMaterialResponseDTO() {
    }

    public StudyMaterialResponseDTO(Integer materialId, String title, String description,
                                     MaterialType materialType, String fileUrl,
                                     Integer uploadedBy, LocalDateTime uploadDate) {
        this.materialId = materialId;
        this.title = title;
        this.description = description;
        this.materialType = materialType;
        this.fileUrl = fileUrl;
        this.uploadedBy = uploadedBy;
        this.uploadDate = uploadDate;
    }

    // Getters and Setters

    public Integer getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Integer materialId) {
        this.materialId = materialId;
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

    public MaterialType getMaterialType() {
        return materialType;
    }

    public void setMaterialType(MaterialType materialType) {
        this.materialType = materialType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Integer getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Integer uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getTargetYearOfStudy() {
        return targetYearOfStudy;
    }

    public void setTargetYearOfStudy(String targetYearOfStudy) {
        this.targetYearOfStudy = targetYearOfStudy;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    @Override
    public String toString() {
        return "StudyMaterialResponseDTO{" +
                "materialId=" + materialId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", materialType=" + materialType +
                ", fileUrl='" + fileUrl + '\'' +
                ", uploadedBy=" + uploadedBy +
                ", targetYearOfStudy='" + targetYearOfStudy + '\'' +
                ", uploadDate=" + uploadDate +
                '}';
    }

    // Static Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer materialId;
        private String title;
        private String description;
        private MaterialType materialType;
        private String fileUrl;
        private Integer uploadedBy;
        private String targetYearOfStudy;
        private LocalDateTime uploadDate;

        public Builder materialId(Integer materialId) {
            this.materialId = materialId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder materialType(MaterialType materialType) {
            this.materialType = materialType;
            return this;
        }

        public Builder fileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
            return this;
        }

        public Builder uploadedBy(Integer uploadedBy) {
            this.uploadedBy = uploadedBy;
            return this;
        }

        public Builder targetYearOfStudy(String targetYearOfStudy) {
            this.targetYearOfStudy = targetYearOfStudy;
            return this;
        }

        public Builder uploadDate(LocalDateTime uploadDate) {
            this.uploadDate = uploadDate;
            return this;
        }

        public StudyMaterialResponseDTO build() {
            StudyMaterialResponseDTO dto = new StudyMaterialResponseDTO(
                    materialId, title, description, materialType,
                    fileUrl, uploadedBy, uploadDate);
            dto.setTargetYearOfStudy(targetYearOfStudy);
            return dto;
        }
    }
}
