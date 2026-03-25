package com.uniconnect.student.modules.mentor.dto;

import com.uniconnect.student.modules.mentor.enums.MentorType;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for mentor connection request.
 */
public class MentorConnectionRequestDTO {

    @NotNull(message = "Student ID is required")
    private Integer studentId;

    @NotNull(message = "Mentor ID is required")
    private Integer mentorId;

    @NotNull(message = "Mentor type is required")
    private MentorType mentorType;

    public MentorConnectionRequestDTO() {
    }

    public MentorConnectionRequestDTO(Integer studentId, Integer mentorId, MentorType mentorType) {
        this.studentId = studentId;
        this.mentorId = mentorId;
        this.mentorType = mentorType;
    }

    // Getters and Setters

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getMentorId() {
        return mentorId;
    }

    public void setMentorId(Integer mentorId) {
        this.mentorId = mentorId;
    }

    public MentorType getMentorType() {
        return mentorType;
    }

    public void setMentorType(MentorType mentorType) {
        this.mentorType = mentorType;
    }

    @Override
    public String toString() {
        return "MentorConnectionRequestDTO{" +
                "studentId=" + studentId +
                ", mentorId=" + mentorId +
                ", mentorType=" + mentorType +
                '}';
    }
}
