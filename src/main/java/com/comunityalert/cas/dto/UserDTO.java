package com.comunityalert.cas.dto;

import java.time.Instant;
import java.util.UUID;
import com.comunityalert.cas.enums.Role;

public class UserDTO {
    private UUID id;
    private String fullName;
    private String email;
    // NO password field - security!
    private String phoneNumber;
    private Role role;
    private Instant createdAt;
    private UUID locationId;
    private String locationName;

    // Default constructor
    public UserDTO() {}

    // Full constructor
    public UserDTO(UUID id, String fullName, String email, String phoneNumber, 
                   Role role, Instant createdAt, UUID locationId, String locationName) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.createdAt = createdAt;
        this.locationId = locationId;
        this.locationName = locationName;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}