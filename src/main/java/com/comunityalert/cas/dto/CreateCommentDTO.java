package com.comunityalert.cas.dto;

import java.util.UUID;

public class CreateCommentDTO {
    private String message;
    private UUID createdById;

    // Constructors
    public CreateCommentDTO() {}

    public CreateCommentDTO(String message, UUID createdById) {
        this.message = message;
        this.createdById = createdById;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public void setCreatedById(UUID createdById) {
        this.createdById = createdById;
    }
}