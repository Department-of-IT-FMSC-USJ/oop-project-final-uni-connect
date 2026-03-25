package com.uniconnect.student.modules.recommendation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or updating a student profile with interest tags.
 */
public class StudentProfileRequestDTO {

    @NotNull(message = "Student ID is required")
    private Integer studentId;

    @NotBlank(message = "Student name is required")
    @Size(max = 150, message = "Student name cannot exceed 150 characters")
    private String studentName;

    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @NotBlank(message = "Interest tags are required")
    @Size(max = 500, message = "Interest tags cannot exceed 500 characters")
    private String interestTags;

    public StudentProfileRequestDTO() {
    }

    public StudentProfileRequestDTO(Integer studentId, String studentName,
                                     String department, String interestTags) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.department = department;
        this.interestTags = interestTags;
    }

    // Getters and Setters

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
        return "StudentProfileRequestDTO{" +
                "studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", department='" + department + '\'' +
                ", interestTags='" + interestTags + '\'' +
                '}';
    }
}
