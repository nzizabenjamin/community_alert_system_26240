package com.comunityalert.cas.dto;

import java.util.UUID;
import com.comunityalert.cas.enums.Role;

public class CreateUserDTO {
    private String fullName;
    private String email;
    private String password;  // Only for creation!
    private String phoneNumber;
    private Role role;
    private UUID locationId;
    private Integer villageCode; // Alternative to locationId - village code from RwandaLocations

    // Constructors
    public CreateUserDTO() {}

    public CreateUserDTO(String fullName, String email, String password, 
                        String phoneNumber, Role role, UUID locationId) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.locationId = locationId;
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }

    public Integer getVillageCode() {
        return villageCode;
    }

    public void setVillageCode(Integer villageCode) {
        this.villageCode = villageCode;
    }
}