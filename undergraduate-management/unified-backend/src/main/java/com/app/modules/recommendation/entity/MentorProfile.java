package com.app.modules.recommendation.entity;

import com.app.modules.recommendation.enums.MentorCategory;
import jakarta.persistence.*;

/**
 * Entity to store a mentor's expertise tags for recommendation matching.
 * Mapped to the 'mentor_profiles' database table.
 */
@Entity
@Table(name = "mentor_profiles")
public class MentorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Integer profileId;

    @Column(name = "mentor_id", nullable = false, unique = true)
    private Integer mentorId;

    @Column(name = "mentor_name", nullable = false, length = 150)
    private String mentorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "mentor_category", nullable = false, length = 50)
    private MentorCategory mentorCategory;

    @Column(name = "expertise_tags", nullable = false, length = 500)
    private String expertiseTags;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    public MentorProfile() {
    }

    public MentorProfile(Integer profileId, Integer mentorId, String mentorName,
                          MentorCategory mentorCategory, String expertiseTags,
                          String department, Boolean isAvailable) {
        this.profileId = profileId;
        this.mentorId = mentorId;
        this.mentorName = mentorName;
        this.mentorCategory = mentorCategory;
        this.expertiseTags = expertiseTags;
        this.department = department;
        this.isAvailable = isAvailable;
    }

    @PrePersist
    protected void onCreate() {
        if (this.isAvailable == null) {
            this.isAvailable = true;
        }
    }

    // Getters and Setters

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

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

    public MentorCategory getMentorCategory() {
        return mentorCategory;
    }

    public void setMentorCategory(MentorCategory mentorCategory) {
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

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    @Override
    public String toString() {
        return "MentorProfile{" +
                "profileId=" + profileId +
                ", mentorId=" + mentorId +
                ", mentorName='" + mentorName + '\'' +
                ", mentorCategory=" + mentorCategory +
                ", expertiseTags='" + expertiseTags + '\'' +
                ", department='" + department + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
