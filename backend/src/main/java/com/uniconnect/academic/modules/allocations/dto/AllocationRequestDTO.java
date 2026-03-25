package com.uniconnect.academic.modules.allocations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for student allocation request.
 */
public class AllocationRequestDTO {

    @NotNull(message = "Student ID is required")
    private Integer studentId;

    @NotNull(message = "Mentor ID is required")
    private Integer mentorId;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    public AllocationRequestDTO() {
    }

    public AllocationRequestDTO(Integer studentId, Integer mentorId, String department) {
        this.studentId = studentId;
        this.mentorId = mentorId;
        this.department = department;
    }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public Integer getMentorId() { return mentorId; }
    public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    @Override
    public String toString() {
        return "AllocationRequestDTO{studentId=" + studentId + ", mentorId=" + mentorId +
                ", department='" + department + "'}";
    }
}
