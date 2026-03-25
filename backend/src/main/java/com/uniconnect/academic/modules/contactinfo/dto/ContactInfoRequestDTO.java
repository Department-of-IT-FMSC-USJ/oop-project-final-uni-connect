package com.uniconnect.academic.modules.contactinfo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for mentor contact information request.
 */
public class ContactInfoRequestDTO {

    @NotNull(message = "Mentor ID is required")
    private Integer mentorId;

    @NotBlank(message = "Mentor name is required")
    @Size(max = 100, message = "Mentor name cannot exceed 100 characters")
    private String mentorName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    private String phone;

    @Size(max = 100, message = "Office location cannot exceed 100 characters")
    private String officeLocation;

    @Size(max = 100, message = "Office hours cannot exceed 100 characters")
    private String officeHours;

    public ContactInfoRequestDTO() {
    }

    public ContactInfoRequestDTO(Integer mentorId, String mentorName, String email,
                                  String phone, String officeLocation, String officeHours) {
        this.mentorId = mentorId;
        this.mentorName = mentorName;
        this.email = email;
        this.phone = phone;
        this.officeLocation = officeLocation;
        this.officeHours = officeHours;
    }

    public Integer getMentorId() { return mentorId; }
    public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }
    public String getMentorName() { return mentorName; }
    public void setMentorName(String mentorName) { this.mentorName = mentorName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getOfficeLocation() { return officeLocation; }
    public void setOfficeLocation(String officeLocation) { this.officeLocation = officeLocation; }
    public String getOfficeHours() { return officeHours; }
    public void setOfficeHours(String officeHours) { this.officeHours = officeHours; }

    @Override
    public String toString() {
        return "ContactInfoRequestDTO{mentorId=" + mentorId + ", mentorName='" + mentorName +
                "', email='" + email + "'}";
    }
}
