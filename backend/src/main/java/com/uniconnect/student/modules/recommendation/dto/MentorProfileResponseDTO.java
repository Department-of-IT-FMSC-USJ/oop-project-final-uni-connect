package com.uniconnect.student.modules.recommendation.dto;

/**
 * DTO for retrieving a mentor's profile information.
 */
public class MentorProfileResponseDTO {

    private Integer mentorId;
    private String mentorName;
    private String mentorCategory;
    private String expertiseTags;
    private String department;
    private String company;
    private Boolean isAvailable;

    public MentorProfileResponseDTO() {
    }

    public MentorProfileResponseDTO(Integer mentorId, String mentorName, String mentorCategory,
                                    String expertiseTags, String department, String company, Boolean isAvailable) {
        this.mentorId = mentorId;
        this.mentorName = mentorName;
        this.mentorCategory = mentorCategory;
        this.expertiseTags = expertiseTags;
        this.department = department;
        this.company = company;
        this.isAvailable = isAvailable;
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    @Override
    public String toString() {
        return "MentorProfileResponseDTO{" +
                "mentorId=" + mentorId +
                ", mentorName='" + mentorName + '\'' +
                ", mentorCategory='" + mentorCategory + '\'' +
                ", expertiseTags='" + expertiseTags + '\'' +
                ", department='" + department + '\'' +
                ", company='" + company + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
