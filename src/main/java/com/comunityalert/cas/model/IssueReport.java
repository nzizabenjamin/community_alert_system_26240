package com.comunityalert.cas.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.comunityalert.cas.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "issues")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IssueReport {
    
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category")
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = true)
    @JsonIgnoreProperties({"parent", "children"})
    private Location location;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "date_reported")
    private Instant dateReported = Instant.now();

    @Column(name = "date_resolved")
    private Instant dateResolved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = true)
    @JsonIgnoreProperties({"password", "email", "role"})
    private User reportedBy;

    // NEW: Many-to-Many relationship with Tags
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "issue_tags",
        joinColumns = @JoinColumn(name = "issue_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnoreProperties({"issues"})
    private Set<Tag> tags = new HashSet<>();

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Instant getDateReported() {
        return dateReported;
    }

    public void setDateReported(Instant dateReported) {
        this.dateReported = dateReported;
    }

    public Instant getDateResolved() {
        return dateResolved;
    }

    public void setDateResolved(Instant dateResolved) {
        this.dateResolved = dateResolved;
    }

    public User getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    // Helper methods for managing tags
    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getIssues().add(this);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getIssues().remove(this);
    }
}