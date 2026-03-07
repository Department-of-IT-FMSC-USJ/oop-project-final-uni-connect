package com.uniconnect.entity;

import com.uniconnect.enums.MaterialType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class mapped to the 'study_materials' database table.
 */
@Entity
@Table(name = "study_materials")
public class StudyMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Integer materialId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false)
    private MaterialType materialType;

    @Column(name = "file_url", nullable = false, length = 255)
    private String fileUrl;

    @Column(name = "uploaded_by", nullable = false)
    private Integer uploadedBy;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    public StudyMaterial() {
    }

    public StudyMaterial(Integer materialId, String title, String description,
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

    @PrePersist
    protected void onCreate() {
        this.uploadDate = LocalDateTime.now();
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

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    @Override
    public String toString() {
        return "StudyMaterial{" +
                "materialId=" + materialId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", materialType=" + materialType +
                ", fileUrl='" + fileUrl + '\'' +
                ", uploadedBy=" + uploadedBy +
                ", uploadDate=" + uploadDate +
                '}';
    }
}
