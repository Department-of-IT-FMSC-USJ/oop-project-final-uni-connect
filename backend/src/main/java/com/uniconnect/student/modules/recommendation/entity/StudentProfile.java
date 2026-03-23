package com.uniconnect.student.modules.recommendation.entity;

import jakarta.persistence.*;

/**
 * Entity to store a student's interest tags for recommendation matching.
 * Mapped to the 'student_profiles' database table.
 */
@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Integer profileId;

    @Column(name = "student_id", nullable = false, unique = true)
    private Integer studentId;

    @Column(name = "student_name", nullable = false, length = 150)
    private String studentName;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "interest_tags", nullable = false, length = 500)
    private String interestTags;

    public StudentProfile() {
    }

    public StudentProfile(Integer profileId, Integer studentId, String studentName,
                           String department, String interestTags) {
        this.profileId = profileId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.department = department;
        this.interestTags = interestTags;
    }

    // Getters and Setters

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getInterestTags() {
        return interestTags;
    }

    public void setInterestTags(String interestTags) {
        this.interestTags = interestTags;
    }

    @Override
    public String toString() {
        return "StudentProfile{" +
                "profileId=" + profileId +
                ", studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", department='" + department + '\'' +
                ", interestTags='" + interestTags + '\'' +
                '}';
    }
}
