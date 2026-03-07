package com.uniconnect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProfileUpdateRequest {
    @JsonProperty("fullName")
    private String fullName;

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getCpmNumber() {
        return cpmNumber;
    }

    public void setCpmNumber(String cpmNumber) {
        this.cpmNumber = cpmNumber;
    }

    public String getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(String yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }
}
