package com.uniconnect.academic.modules.contactinfo.entity;

import jakarta.persistence.*;

/**
 * Entity class mapped to the 'mentor_contact_info' database table.
 */
@Entity
@Table(name = "mentor_contact_info")
public class MentorContactInfo {

    @Id
    @Column(name = "mentor_id")
    private Integer mentorId;

    @Column(name = "mentor_name", nullable = false, length = 100)
    private String mentorName;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "office_location", length = 100)
    private String officeLocation;

    @Column(name = "office_hours", length = 100)
    private String officeHours;

    public MentorContactInfo() {
    }

    public MentorContactInfo(Integer mentorId, String mentorName, String email,
                              String phone, String officeLocation, String officeHours) {
        this.mentorId = mentorId;
        this.mentorName = mentorName;
        this.email = email;
        this.phone = phone;
        this.officeLocation = officeLocation;
        this.officeHours = officeHours;
    }

    // Getters and Setters

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
        return "MentorContactInfo{" +
                "mentorId=" + mentorId +
                ", mentorName='" + mentorName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", officeLocation='" + officeLocation + '\'' +
                ", officeHours='" + officeHours + '\'' +
                '}';
    }
}
