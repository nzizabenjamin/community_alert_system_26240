package com.comunityalert.cas.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "tags")
public class Tag {
    
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "active", nullable = true)
    private Boolean active = true; // Tags are active by default (nullable to allow Hibernate to add column)

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore  // Prevent circular reference in JSON
    private Set<IssueReport> issues = new HashSet<>();

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<IssueReport> getIssues() {
        return issues;
    }

    public void setIssues(Set<IssueReport> issues) {
        this.issues = issues;
    }

    public boolean isActive() {
        return active != null ? active : true; // Default to true if null
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Boolean getActive() {
        return active != null ? active : true;
    }
}