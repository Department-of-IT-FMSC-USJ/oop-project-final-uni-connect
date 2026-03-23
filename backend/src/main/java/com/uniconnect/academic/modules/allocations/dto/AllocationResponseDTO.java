package com.uniconnect.academic.modules.allocations.dto;

import java.time.LocalDateTime;

/**
 * DTO for student allocation response.
 */
public class AllocationResponseDTO {

    private Integer allocationId;
    private Integer mentorId;
    private Integer studentId;
    private String studentName;
    private String department;
    private Integer academicYear;
    private LocalDateTime allocationDate;

    public AllocationResponseDTO() {
    }

    public AllocationResponseDTO(Integer allocationId, Integer mentorId, Integer studentId,
                                  String studentName, String department, Integer academicYear,
                                  LocalDateTime allocationDate) {
        this.allocationId = allocationId;
        this.mentorId = mentorId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.department = department;
        this.academicYear = academicYear;
        this.allocationDate = allocationDate;
    }

    // Getters and Setters

    public Integer getAllocationId() { return allocationId; }
    public void setAllocationId(Integer allocationId) { this.allocationId = allocationId; }
    public Integer getMentorId() { return mentorId; }
    public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getAcademicYear() { return academicYear; }
    public void setAcademicYear(Integer academicYear) { this.academicYear = academicYear; }
    public LocalDateTime getAllocationDate() { return allocationDate; }
    public void setAllocationDate(LocalDateTime allocationDate) { this.allocationDate = allocationDate; }

    @Override
    public String toString() {
        return "AllocationResponseDTO{" +
                "allocationId=" + allocationId +
                ", mentorId=" + mentorId +
                ", studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", department='" + department + '\'' +
                ", academicYear=" + academicYear +
                ", allocationDate=" + allocationDate +
                '}';
    }

    // Static Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer allocationId;
        private Integer mentorId;
        private Integer studentId;
        private String studentName;
        private String department;
        private Integer academicYear;
        private LocalDateTime allocationDate;

        public Builder allocationId(Integer allocationId) { this.allocationId = allocationId; return this; }
        public Builder mentorId(Integer mentorId) { this.mentorId = mentorId; return this; }
        public Builder studentId(Integer studentId) { this.studentId = studentId; return this; }
        public Builder studentName(String studentName) { this.studentName = studentName; return this; }
        public Builder department(String department) { this.department = department; return this; }
        public Builder academicYear(Integer academicYear) { this.academicYear = academicYear; return this; }
        public Builder allocationDate(LocalDateTime allocationDate) { this.allocationDate = allocationDate; return this; }

        public AllocationResponseDTO build() {
            return new AllocationResponseDTO(allocationId, mentorId, studentId,
                    studentName, department, academicYear, allocationDate);
        }
    }
}
