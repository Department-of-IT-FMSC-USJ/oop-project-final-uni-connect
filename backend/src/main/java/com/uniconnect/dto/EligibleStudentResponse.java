package com.uniconnect.dto;

import java.util.List;

public class EligibleStudentResponse {
    private Long studentId;
    private String fullName;
    private String department;
    private Integer totalPoints;
    private Boolean mentorEligible;
    private List<ProofSummary> proofs;

    public EligibleStudentResponse(Long studentId, String fullName, String department, Integer totalPoints,
                                   Boolean mentorEligible, List<ProofSummary> proofs) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.department = department;
        this.totalPoints = totalPoints;
        this.mentorEligible = mentorEligible;
        this.proofs = proofs;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDepartment() {
        return department;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public Boolean getMentorEligible() {
        return mentorEligible;
    }

    public List<ProofSummary> getProofs() {
        return proofs;
    }
}
