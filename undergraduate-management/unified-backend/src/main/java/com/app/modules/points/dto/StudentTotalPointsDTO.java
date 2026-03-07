package com.app.modules.points.dto;

/**
 * DTO for student total points response.
 */
public class StudentTotalPointsDTO {

    private Integer studentId;
    private Integer totalPoints;

    public StudentTotalPointsDTO() {
    }

    public StudentTotalPointsDTO(Integer studentId, Integer totalPoints) {
        this.studentId = studentId;
        this.totalPoints = totalPoints;
    }

    // Getters and Setters

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    @Override
    public String toString() {
        return "StudentTotalPointsDTO{" +
                "studentId=" + studentId +
                ", totalPoints=" + totalPoints +
                '}';
    }
}
