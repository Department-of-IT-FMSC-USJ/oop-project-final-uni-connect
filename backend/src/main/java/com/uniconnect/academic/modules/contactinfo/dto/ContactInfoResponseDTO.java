package com.uniconnect.academic.modules.contactinfo.dto;

/**
 * DTO for mentor contact information response.
 */
public class ContactInfoResponseDTO {

    private Integer mentorId;
    private String mentorName;
    private String email;
    private String phone;
    private String officeLocation;
    private String officeHours;

    public ContactInfoResponseDTO() {
    }

    public ContactInfoResponseDTO(Integer mentorId, String mentorName, String email,
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
        return "ContactInfoResponseDTO{mentorId=" + mentorId + ", mentorName='" + mentorName +
                "', email='" + email + "'}";
    }

    // Static Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer mentorId;
        private String mentorName;
        private String email;
        private String phone;
        private String officeLocation;
        private String officeHours;

        public Builder mentorId(Integer mentorId) { this.mentorId = mentorId; return this; }
        public Builder mentorName(String mentorName) { this.mentorName = mentorName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder officeLocation(String officeLocation) { this.officeLocation = officeLocation; return this; }
        public Builder officeHours(String officeHours) { this.officeHours = officeHours; return this; }

        public ContactInfoResponseDTO build() {
            return new ContactInfoResponseDTO(mentorId, mentorName, email, phone, officeLocation, officeHours);
        }
    }
}
