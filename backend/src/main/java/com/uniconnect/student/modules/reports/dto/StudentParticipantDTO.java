package com.uniconnect.student.modules.reports.dto;

public class StudentParticipantDTO {

    private Long studentId;
    private String fullName;
    private String registrationNumber;
    private String cpmNumber;
    private String yearOfStudy;
    private String department;

    public StudentParticipantDTO() {}

    public StudentParticipantDTO(Long studentId, String fullName, String registrationNumber,
                                  String cpmNumber, String yearOfStudy, String department) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.registrationNumber = registrationNumber;
        this.cpmNumber = cpmNumber;
        this.yearOfStudy = yearOfStudy;
        this.department = department;
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getCpmNumber() { return cpmNumber; }
    public void setCpmNumber(String cpmNumber) { this.cpmNumber = cpmNumber; }

    public String getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(String yearOfStudy) { this.yearOfStudy = yearOfStudy; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
