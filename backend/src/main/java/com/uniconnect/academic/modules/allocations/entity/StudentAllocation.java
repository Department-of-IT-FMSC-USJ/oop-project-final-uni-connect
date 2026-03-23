package com.uniconnect.academic.modules.allocations.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class mapped to the 'student_allocations' database table.
 */
@Entity
@Table(name = "student_allocations")
public class StudentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allocation_id")
    private Integer allocationId;

    @Column(name = "mentor_id", nullable = false)
    private Integer mentorId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "allocation_date", nullable = false)
    private LocalDateTime allocationDate;

    public StudentAllocation() {
    }

    public StudentAllocation(Integer allocationId, Integer mentorId, Integer studentId,
                              String department, LocalDateTime allocationDate) {
        this.allocationId = allocationId;
        this.mentorId = mentorId;
        this.studentId = studentId;
        this.department = department;
        this.allocationDate = allocationDate;
    }

    @PrePersist
    protected void onCreate() {
        this.allocationDate = LocalDateTime.now();
    }

    // Getters and Setters

    public Integer getAllocationId() { return allocationId; }
    public void setAllocationId(Integer allocationId) { this.allocationId = allocationId; }
    public Integer getMentorId() { return mentorId; }
    public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public LocalDateTime getAllocationDate() { return allocationDate; }
    public void setAllocationDate(LocalDateTime allocationDate) { this.allocationDate = allocationDate; }

    @Override
    public String toString() {
        return "StudentAllocation{" +
                "allocationId=" + allocationId +
                ", mentorId=" + mentorId +
                ", studentId=" + studentId +
                ", department='" + department + '\'' +
                ", allocationDate=" + allocationDate +
                '}';
    }
}
