package com.comunityalert.cas.service;

import java.time.Instant;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.comunityalert.cas.model.IssueReport;
import com.comunityalert.cas.model.Tag;
import com.comunityalert.cas.model.Location;
import com.comunityalert.cas.model.User;
import com.comunityalert.cas.enums.Status;
import com.comunityalert.cas.repository.IssueRepository;
import com.comunityalert.cas.repository.LocationRepository;
import com.comunityalert.cas.repository.UserRepository;

@Service
public class IssueService {
    
    private final IssueRepository repo;
    private final TagService tagService;
    private final LocationRepository locationRepo;
    private final UserRepository userRepo;

    public IssueService(IssueRepository repo, TagService tagService, 
                       LocationRepository locationRepo, UserRepository userRepo) { 
        this.repo = repo;
        this.tagService = tagService;
        this.locationRepo = locationRepo;
        this.userRepo = userRepo;
    }

    public IssueReport create(IssueReport i) {
        // Fetch and set the actual Location entity if ID is provided
        if (i.getLocation() != null && i.getLocation().getId() != null) {
            Location location = locationRepo.findById(i.getLocation().getId())
                .orElseThrow(() -> new RuntimeException("Location not found"));
            i.setLocation(location);
        }
        
        // Fetch and set the actual User entity if ID is provided
        if (i.getReportedBy() != null && i.getReportedBy().getId() != null) {
            User user = userRepo.findById(i.getReportedBy().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            i.setReportedBy(user);
        }
        
        i.setDateReported(Instant.now()); 
        i.setStatus(Status.REPORTED); 
        return repo.save(i); 
    }

    /**
     * Create issue from DTO (cleaner approach)
     */
    public IssueReport createFromDTO(com.comunityalert.cas.dto.CreateIssueDTO dto) {
        IssueReport issue = new IssueReport();
        issue.setTitle(dto.getTitle());
        issue.setDescription(dto.getDescription());
        issue.setCategory(dto.getCategory());
        issue.setPhotoUrl(dto.getPhotoUrl());
        
        // Fetch location
        Location location = locationRepo.findById(dto.getLocationId())
            .orElseThrow(() -> new RuntimeException("Location not found"));
        issue.setLocation(location);
        
        // Fetch user
        User user = userRepo.findById(dto.getReportedById())
            .orElseThrow(() -> new RuntimeException("User not found"));
        issue.setReportedBy(user);
        
        issue.setDateReported(Instant.now());
        issue.setStatus(Status.REPORTED);
        
        return repo.save(issue);
    }

    public List<IssueReport> getAll() { 
        return repo.findAll(); 
    }

    public Page<IssueReport> getAll(Pageable pageable) { 
        return repo.findAll(pageable); 
    }

    public Optional<IssueReport> getById(UUID id) {
        return repo.findById(id); 
    }

    public List<IssueReport> getByUser(UUID userId) { 
        return repo.findByReportedById(userId); 
    }

    public IssueReport updateStatus(UUID id, Status status) { 
        IssueReport e = repo.findById(id).orElseThrow(); 
        e.setStatus(status); 
        if (status == Status.RESOLVED) 
            e.setDateResolved(Instant.now()); 
        return repo.save(e); 
    }

    public IssueReport update(UUID id, IssueReport payload) { 
        IssueReport e = repo.findById(id).orElseThrow();
        e.setTitle(payload.getTitle()); 
        e.setDescription(payload.getDescription()); 
        e.setCategory(payload.getCategory()); 
        e.setLocation(payload.getLocation()); 
        return repo.save(e); 
    }

    public void delete(UUID id) { 
        repo.deleteById(id); 
    }

    /**
     * Add a tag to an issue
     */
    public IssueReport addTag(UUID issueId, UUID tagId) {
        IssueReport issue = repo.findById(issueId)
            .orElseThrow(() -> new RuntimeException("Issue not found"));
        Tag tag = tagService.getById(tagId)
            .orElseThrow(() -> new RuntimeException("Tag not found"));
        
        issue.addTag(tag);
        return repo.save(issue);
    }

    /**
     * Remove a tag from an issue
     */
    public IssueReport removeTag(UUID issueId, UUID tagId) {
        IssueReport issue = repo.findById(issueId)
            .orElseThrow(() -> new RuntimeException("Issue not found"));
        Tag tag = tagService.getById(tagId)
            .orElseThrow(() -> new RuntimeException("Tag not found"));
        
        issue.removeTag(tag);
        return repo.save(issue);
    }

    /**
     * Get all tags for an issue
     */
    public Set<Tag> getIssueTags(UUID issueId) {
        IssueReport issue = repo.findById(issueId)
            .orElseThrow(() -> new RuntimeException("Issue not found"));
        return issue.getTags();
    }
}