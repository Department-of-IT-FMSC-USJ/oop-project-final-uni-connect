package com.uniconnect.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @Column(unique = true, nullable = false)
    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;

    @ElementCollection
    @CollectionTable(name = "department_cpm_numbers", joinColumns = @JoinColumn(name = "department_id"))
    @Column(name = "cpm_number")
    @JsonProperty("cpmNumbers")
    private List<String> cpmNumbers;

    // Constructors
    public Department() {
    }

    public Department(String name, String code) {
        this.name = name;
        this.code = code;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<String> getCpmNumbers() {
        return cpmNumbers;
    }

    public void setCpmNumbers(List<String> cpmNumbers) {
        this.cpmNumbers = cpmNumbers;
    }

    public boolean isCpmNumberValid(String cpmNumber) {
        return cpmNumbers != null && cpmNumbers.contains(cpmNumber);
    }
}
