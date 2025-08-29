package org.example.medicaldataservice.dto;

import org.example.medicaldataservice.model.Role;

public class StaffDTO {
    private long id;
    private String SSN;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;

    public StaffDTO() {
    }

    public StaffDTO(long id, String SSN, String firstName, String lastName, String email, Role role) {
        this.id = id;
        this.SSN = SSN;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSSN() {
        return SSN;
    }

    public void setSSN(String SSN) {
        this.SSN = SSN;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
