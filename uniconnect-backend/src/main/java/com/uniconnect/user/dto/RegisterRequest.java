package com.uniconnect.user.dto;

import com.uniconnect.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterRequest {
    @NotBlank
    @JsonProperty("fullName")
    private String fullName;

    @Email
    @NotBlank
    @JsonProperty("email")
    private String email;

    @NotBlank
    @JsonProperty("password")
    private String password;

    @JsonProperty("role")
    private Role role;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("department")
    private String department;

    @JsonProperty("profilePicture")
    private String profilePicture;
    @JsonProperty("registrationNumber")
    private String registrationNumber;
    @JsonProperty("cpmNumber")
    private String cpmNumber;
    @JsonProperty("yearOfStudy")
    private String yearOfStudy;

    public RegisterRequest() {
    }

    public RegisterRequest(String fullName, String email, String password, Role role, String phone, String department,
            String profilePicture, String registrationNumber, String cpmNumber, String yearOfStudy) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phone = phone;
        this.department = department;
        this.profilePicture = profilePicture;
        this.registrationNumber = registrationNumber;
        this.cpmNumber = cpmNumber;
        this.yearOfStudy = yearOfStudy;
    }

    // Getters
    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public String getPhone() {
        return phone;
    }

    public String getDepartment() {
        return department;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getCpmNumber() {
        return cpmNumber;
    }

    public String getYearOfStudy() {
        return yearOfStudy;
    }

    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public void setCpmNumber(String cpmNumber) {
        this.cpmNumber = cpmNumber;
    }

    public void setYearOfStudy(String yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }
}
