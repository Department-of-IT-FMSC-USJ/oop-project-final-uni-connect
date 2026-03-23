package com.uniconnect.student.modules.recommendation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or updating a mentor profile with expertise tags.
 */
public class MentorProfileRequestDTO {

    @NotNull(message = "Mentor ID is required")
    private Integer mentorId;

    @NotBlank(message = "Mentor name is required")
    @Size(max = 150, message = "Mentor name cannot exceed 150 characters")
    private String mentorName;

    @NotBlank(message = "Mentor category is required")
    private String mentorCategory;

    @NotBlank(message = "Expertise tags are required")
    @Size(max = 500, message = "Expertise tags cannot exceed 500 characters")
    private String expertiseTags;

    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    public MentorProfileRequestDTO() {
    }

    public MentorProfileRequestDTO(Integer mentorId, String mentorName, String mentorCategory,
                                    String expertiseTags, String department) {
        this.mentorId = mentorId;
        this.mentorName = mentorName;
        this.mentorCategory = mentorCategory;
        this.expertiseTags = expertiseTags;
        this.department = department;
    }

    // Getters and Setters

    public Integer getMentorId() {
        return mentorId;
    }

    public void setMentorId(Integer mentorId) {
        this.mentorId = mentorId;
    }

    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }

    public String getMentorCategory() {
        return mentorCategory;
    }

    public void setMentorCategory(String mentorCategory) {
        this.mentorCategory = mentorCategory;
    }

    public String getExpertiseTags() {
        return expertiseTags;
    }

    public void setExpertiseTags(String expertiseTags) {
        this.expertiseTags = expertiseTags;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "MentorProfileRequestDTO{" +
                "mentorId=" + mentorId +
                ", mentorName='" + mentorName + '\'' +
                ", mentorCategory='" + mentorCategory + '\'' +
                ", expertiseTags='" + expertiseTags + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
