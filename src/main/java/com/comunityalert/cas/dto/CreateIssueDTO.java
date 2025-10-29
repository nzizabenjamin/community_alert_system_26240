package com.comunityalert.cas.dto;

import java.util.UUID;

public class CreateIssueDTO {
    private String title;
    private String description;
    private String category;
    private UUID locationId;
    private UUID reportedById;
    private String photoUrl;

    // Constructors
    public CreateIssueDTO() {}

    public CreateIssueDTO(String title, String description, String category, 
                         UUID locationId, UUID reportedById, String photoUrl) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.locationId = locationId;
        this.reportedById = reportedById;
        this.photoUrl = photoUrl;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }

    public UUID getReportedById() {
        return reportedById;
    }

    public void setReportedById(UUID reportedById) {
        this.reportedById = reportedById;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}