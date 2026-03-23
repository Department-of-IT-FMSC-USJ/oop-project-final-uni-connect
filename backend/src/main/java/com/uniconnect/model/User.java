package com.uniconnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    public User() {
    }

    public User(Long id, String fullName, String email, String password, Role role, String phone, String department,
            String profilePicture, String registrationNumber, String cpmNumber, String yearOfStudy,
            Integer cumulativePoints, Boolean mentorEligible, Long managedByDepartmentHeadId, Boolean active) {
        this.id = id;
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
        this.cumulativePoints = cumulativePoints;
        this.mentorEligible = mentorEligible;
        this.managedByDepartmentHeadId = managedByDepartmentHeadId;
        this.active = active;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @NotBlank
    @JsonProperty("fullName")
    private String fullName;

    @Email
    @NotBlank
    @Column(unique = true)
    @JsonProperty("email")
    private String email;

    @NotBlank
    @JsonProperty("password")
    private String password;

    @Enumerated(EnumType.STRING)
    @JsonProperty("role")
    private Role role;

    @Lob
    @Column(columnDefinition = "CLOB")
    @JsonProperty("profilePicture")
    private String profilePicture;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("department")
    private String department;

    @Column(name = "registration_number")
    @JsonProperty("registrationNumber")
    private String registrationNumber;

    @Column(name = "cpm_number")
    @JsonProperty("cpmNumber")
    private String cpmNumber;

    @Column(name = "year_of_study")
    @JsonProperty("yearOfStudy")
    private String yearOfStudy;

    @Column(name = "cumulative_points")
    @JsonProperty("cumulativePoints")
    private Integer cumulativePoints = 0;

    @Column(name = "mentor_eligible")
    @JsonProperty("mentorEligible")
    private Boolean mentorEligible = false;

    @Column(name = "managed_by_department_head_id")
    @JsonProperty("managedByDepartmentHeadId")
    private Long managedByDepartmentHeadId;

    @Column(name = "active")
    @JsonProperty("active")
    private Boolean active = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
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

    public Integer getCumulativePoints() {
        return cumulativePoints;
    }

    public void setCumulativePoints(Integer cumulativePoints) {
        this.cumulativePoints = cumulativePoints;
    }

    public Boolean getMentorEligible() {
        return mentorEligible;
    }

    public void setMentorEligible(Boolean mentorEligible) {
        this.mentorEligible = mentorEligible;
    }

    public Long getManagedByDepartmentHeadId() {
        return managedByDepartmentHeadId;
    }

    public void setManagedByDepartmentHeadId(Long managedByDepartmentHeadId) {
        this.managedByDepartmentHeadId = managedByDepartmentHeadId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public static class UserBuilder {
        private String fullName;
        private String email;
        private String password;
        private Role role;
        private String phone;
        private String department;
        private String profilePicture;
        private String registrationNumber;
        private String cpmNumber;
        private String yearOfStudy;
        private Integer cumulativePoints;
        private Boolean mentorEligible;
        private Long managedByDepartmentHeadId;
        private Boolean active;

        UserBuilder() {
        }

        public UserBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UserBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserBuilder department(String department) {
            this.department = department;
            return this;
        }

        public UserBuilder profilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
            return this;
        }

        public UserBuilder registrationNumber(String registrationNumber) {
            this.registrationNumber = registrationNumber;
            return this;
        }

        public UserBuilder cpmNumber(String cpmNumber) {
            this.cpmNumber = cpmNumber;
            return this;
        }

        public UserBuilder yearOfStudy(String yearOfStudy) {
            this.yearOfStudy = yearOfStudy;
            return this;
        }

        public UserBuilder cumulativePoints(Integer cumulativePoints) {
            this.cumulativePoints = cumulativePoints;
            return this;
        }

        public UserBuilder mentorEligible(Boolean mentorEligible) {
            this.mentorEligible = mentorEligible;
            return this;
        }

        public UserBuilder managedByDepartmentHeadId(Long managedByDepartmentHeadId) {
            this.managedByDepartmentHeadId = managedByDepartmentHeadId;
            return this;
        }

        public UserBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public User build() {
            Integer safePoints = cumulativePoints == null ? 0 : cumulativePoints;
            Boolean safeEligible = mentorEligible != null && mentorEligible;
            Boolean safeActive = active == null || active;
            return new User(null, fullName, email, password, role, phone, department, profilePicture,
                    registrationNumber, cpmNumber, yearOfStudy, safePoints, safeEligible, managedByDepartmentHeadId, safeActive);
        }
    }
}
