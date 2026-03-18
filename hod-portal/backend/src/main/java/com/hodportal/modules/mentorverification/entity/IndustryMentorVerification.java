package com.hodportal.modules.mentorverification.entity;

import com.hodportal.modules.mentorverification.enums.VerificationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class mapped to the 'industry_mentor_verifications' database table.
 */
@Entity
@Table(name = "industry_mentor_verifications")
public class IndustryMentorVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentor_id")
    private Integer mentorId;

    @Column(name = "mentor_name", nullable = false, length = 150)
    private String mentorName;

    @Column(name = "organization", nullable = false, length = 150)
    private String organization;

    @Column(name = "professional_expertise", columnDefinition = "TEXT")
    private String professionalExpertise;

    @Column(name = "contact_email", nullable = false, length = 150)
    private String contactEmail;

    @Column(name = "hod_id")
    private Integer hodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public IndustryMentorVerification() {
    }

    public IndustryMentorVerification(Integer mentorId, String mentorName, String organization,
                                       String professionalExpertise, String contactEmail,
                                       Integer hodId, VerificationStatus verificationStatus,
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

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.verificationStatus == null) {
            this.verificationStatus = VerificationStatus.PENDING;
        }
    }

    // Getters and Setters

    public Integer getMentorId() {
        return mentorId;
    }

    public void setMentorId(Integer mentorId) {
        this.mentorId = mentorId;
    }

    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getProfessionalExpertise() {
        return professionalExpertise;
    }

    public void setProfessionalExpertise(String professionalExpertise) {
        this.professionalExpertise = professionalExpertise;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Integer getHodId() {
        return hodId;
    }

    public void setHodId(Integer hodId) {
        this.hodId = hodId;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getVerificationDate() {
        return verificationDate;
    }

    public void setVerificationDate(LocalDateTime verificationDate) {
        this.verificationDate = verificationDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "IndustryMentorVerification{" +
                "mentorId=" + mentorId +
                ", mentorName='" + mentorName + '\'' +
                ", organization='" + organization + '\'' +
                ", professionalExpertise='" + professionalExpertise + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", hodId=" + hodId +
                ", verificationStatus=" + verificationStatus +
                ", verificationDate=" + verificationDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
