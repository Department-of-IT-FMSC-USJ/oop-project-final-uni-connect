package com.hodportal.modules.mentorverification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for industry mentor verification request (used for updating verification status).
 */
public class VerificationRequestDTO {

    @NotBlank(message = "Verification status is required")
    private String verificationStatus;

    public VerificationRequestDTO() {
    }

    public VerificationRequestDTO(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    // Getters and Setters

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    @Override
    public String toString() {
        return "VerificationRequestDTO{" +
                "verificationStatus='" + verificationStatus + '\'' +
                '}';
    }
}
