package com.uniconnect.hod.modules.mentorverification.dto;

import java.time.LocalDateTime;

/**
 * DTO for industry mentor verification response.
 */
public class VerificationResponseDTO {

    private Integer mentorId;
    private String mentorName;
    private String organization;
    private String professionalExpertise;
    private String contactEmail;
    private Integer hodId;
    private String verificationStatus;
    private LocalDateTime verificationDate;
    private LocalDateTime createdAt;

    public VerificationResponseDTO() {
    }

    public VerificationResponseDTO(Integer mentorId, String mentorName, String organization,
                                    String professionalExpertise, String contactEmail,
                                    Integer hodId, String verificationStatus,
                                    LocalDateTime verificationDate, LocalDateTime createdAt) {
        this.mentorId = mentorId;
        this.mentorName = mentorName;
        this.organization = organization;
        this.professionalExpertise = professionalExpertise;
        this.contactEmail = contactEmail;
        this.hodId = hodId;
        this.verificationStatus = verificationStatus;
        this.verificationDate = verificationDate;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public Integer getMentorId() { return mentorId; }
    public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }
    public String getMentorName() { return mentorName; }
    public void setMentorName(String mentorName) { this.mentorName = mentorName; }
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    public String getProfessionalExpertise() { return professionalExpertise; }
    public void setProfessionalExpertise(String professionalExpertise) { this.professionalExpertise = professionalExpertise; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public Integer getHodId() { return hodId; }
    public void setHodId(Integer hodId) { this.hodId = hodId; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    public LocalDateTime getVerificationDate() { return verificationDate; }
    public void setVerificationDate(LocalDateTime verificationDate) { this.verificationDate = verificationDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "VerificationResponseDTO{" +
                "mentorId=" + mentorId +
                ", mentorName='" + mentorName + '\'' +
                ", organization='" + organization + '\'' +
                ", professionalExpertise='" + professionalExpertise + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", hodId=" + hodId +
                ", verificationStatus='" + verificationStatus + '\'' +
                ", verificationDate=" + verificationDate +
                ", createdAt=" + createdAt +
                '}';
    }

    // Static Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer mentorId;
        private String mentorName;
        private String organization;
        private String professionalExpertise;
        private String contactEmail;
        private Integer hodId;
        private String verificationStatus;
        private LocalDateTime verificationDate;
        private LocalDateTime createdAt;

        public Builder mentorId(Integer mentorId) { this.mentorId = mentorId; return this; }
        public Builder mentorName(String mentorName) { this.mentorName = mentorName; return this; }
        public Builder organization(String organization) { this.organization = organization; return this; }
        public Builder professionalExpertise(String professionalExpertise) { this.professionalExpertise = professionalExpertise; return this; }
        public Builder contactEmail(String contactEmail) { this.contactEmail = contactEmail; return this; }
        public Builder hodId(Integer hodId) { this.hodId = hodId; return this; }
        public Builder verificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; return this; }
        public Builder verificationDate(LocalDateTime verificationDate) { this.verificationDate = verificationDate; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public VerificationResponseDTO build() {
            return new VerificationResponseDTO(mentorId, mentorName, organization,
                    professionalExpertise, contactEmail, hodId, verificationStatus,
                    verificationDate, createdAt);
        }
    }
}
