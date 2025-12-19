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
import com.comunityalert.cas.enums.Role;
import com.comunityalert.cas.repository.IssueRepository;
import com.comunityalert.cas.repository.LocationRepository;
import com.comunityalert.cas.repository.UserRepository;

@Service
public class IssueService {
    
    private final IssueRepository repo;
    private final TagService tagService;
    private final LocationRepository locationRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public IssueService(IssueRepository repo, TagService tagService, 
                       LocationRepository locationRepo, UserRepository userRepo,
                       NotificationService notificationService) { 
        this.repo = repo;
        this.tagService = tagService;
        this.locationRepo = locationRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
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
        
        IssueReport savedIssue = repo.save(i);
        
        // System-generated notification: Notify all ADMIN users about new issue
        notifyAdminsAboutNewIssue(savedIssue);
        
        return savedIssue;
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
        
        // Validate and add tags if provided
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            tagService.validateTagIds(dto.getTagIds());
            for (UUID tagId : dto.getTagIds()) {
                Tag tag = tagService.getById(tagId)
                    .orElseThrow(() -> new RuntimeException("Tag not found"));
                issue.addTag(tag);
            }
        }
        
        issue.setDateReported(Instant.now());
        issue.setStatus(Status.REPORTED);
        
        IssueReport savedIssue = repo.save(issue);
        
        // System-generated notification: Notify all ADMIN users about new issue
        notifyAdminsAboutNewIssue(savedIssue);
        
        return savedIssue;
    }

    /**
     * Notify all ADMIN users when a new issue is created
     */
    private void notifyAdminsAboutNewIssue(IssueReport issue) {
        try {
            List<User> adminUsers = userRepo.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .toList();
            
            String reporterName = issue.getReportedBy() != null 
                ? (issue.getReportedBy().getFullName() != null && !issue.getReportedBy().getFullName().isEmpty()
                    ? issue.getReportedBy().getFullName() 
                    : issue.getReportedBy().getEmail())
                : "Unknown";
            
            String locationName = issue.getLocation() != null 
                ? issue.getLocation().getName() 
                : "Unknown location";
            
            String message = String.format("New issue reported: '%s' by %s in %s", 
                issue.getTitle(), reporterName, locationName);
            
            for (User admin : adminUsers) {
                notificationService.createNotification(admin, issue, message);
            }
        } catch (Exception e) {
            // Log error but don't fail issue creation if notification fails
            System.err.println("Error creating notifications for new issue: " + e.getMessage());
        }
    }

    public List<IssueReport> getAll() { 
        return repo.findAll(); 
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<IssueReport> getAll(Pageable pageable) { 
        try {
            System.out.println("DEBUG IssueService: getAll(Pageable) called with sort: " + pageable.getSort());
            Page<IssueReport> pageData = repo.findAll(pageable);
            System.out.println("DEBUG IssueService: Found " + pageData.getTotalElements() + " total issues");
            // Force load relationships before transaction closes
            pageData.getContent().forEach(issue -> {
                try {
                    // Initialize location proxy
                    if (issue.getLocation() != null) {
                        issue.getLocation().getName(); // Trigger proxy initialization
                        issue.getLocation().getType(); // Ensure it's fully loaded
                    }
                    // Initialize reportedBy proxy
                    if (issue.getReportedBy() != null) {
                        issue.getReportedBy().getId(); // Trigger proxy initialization
                        issue.getReportedBy().getEmail(); // Ensure it's fully loaded
                    }
                    // Initialize tags collection
                    if (issue.getTags() != null) {
                        issue.getTags().size(); // Trigger collection initialization
                        issue.getTags().forEach(tag -> tag.getName()); // Load each tag
                    }
                } catch (Exception e) {
                    System.err.println("DEBUG IssueService: Error loading relationships for issue " + issue.getId() + ": " + e.getMessage());
                }
            });
            return pageData;
        } catch (Exception e) {
            System.err.println("DEBUG IssueService: Error in getAll(Pageable): " + e.getMessage());
            System.err.println("Exception type: " + e.getClass().getName());
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
            e.printStackTrace();
            // Return empty page instead of throwing
            return Page.empty(pageable);
        }
    }

    /**
     * Get all issues with role-based filtering
     * RESIDENT users only see their own issues
     * ADMIN users see all issues
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<IssueReport> getAll(Pageable pageable, User currentUser) {
        if (currentUser == null) {
            // No user authenticated, return empty page
            System.out.println("DEBUG IssueService: No user provided, returning empty page");
            return Page.empty(pageable);
        }
        
        try {
            Page<IssueReport> pageData;
            if (currentUser.getRole() == Role.ADMIN) {
                // Admin sees all issues
                System.out.println("DEBUG IssueService: Admin user, fetching all issues with sort: " + pageable.getSort());
                try {
                    pageData = repo.findAll(pageable);
                } catch (Exception e) {
                    System.err.println("DEBUG IssueService: Error in repo.findAll: " + e.getMessage());
                    System.err.println("Exception type: " + e.getClass().getName());
                    e.printStackTrace();
                    // Return empty page on error
                    return Page.empty(pageable);
                }
            } else {
                // Resident sees only their own issues
                System.out.println("DEBUG IssueService: Resident user, fetching issues for user ID: " + currentUser.getId());
                try {
                    pageData = repo.findByReportedById(currentUser.getId(), pageable);
                } catch (Exception e) {
                    System.err.println("DEBUG IssueService: Error in repo.findByReportedById: " + e.getMessage());
                    System.err.println("Exception type: " + e.getClass().getName());
                    e.printStackTrace();
                    // Return empty page on error
                    return Page.empty(pageable);
                }
            }
            
            System.out.println("DEBUG IssueService: Found " + pageData.getTotalElements() + " total issues, " + pageData.getContent().size() + " on this page");
            
            // Force load relationships before transaction closes
            pageData.getContent().forEach(issue -> {
                try {
                    // Initialize location proxy
                    if (issue.getLocation() != null) {
                        issue.getLocation().getName(); // Trigger proxy initialization
                        issue.getLocation().getType(); // Ensure it's fully loaded
                    }
                    // Initialize reportedBy proxy
                    if (issue.getReportedBy() != null) {
                        issue.getReportedBy().getId(); // Trigger proxy initialization
                        issue.getReportedBy().getEmail(); // Ensure it's fully loaded
                    }
                    // Initialize tags collection
                    if (issue.getTags() != null) {
                        issue.getTags().size(); // Trigger collection initialization
                        issue.getTags().forEach(tag -> tag.getName()); // Load each tag
                    }
                } catch (Exception e) {
                    System.err.println("DEBUG IssueService: Error loading relationships for issue " + issue.getId() + ": " + e.getMessage());
                }
            });
            
            return pageData;
        } catch (Exception e) {
            System.err.println("DEBUG IssueService: Error fetching issues: " + e.getMessage());
            e.printStackTrace();
            // Return empty page on error instead of throwing
            return Page.empty(pageable);
        }
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Optional<IssueReport> getById(UUID id) {
        Optional<IssueReport> issue = repo.findById(id);
        // Force load relationships before transaction closes
        issue.ifPresent(i -> {
            if (i.getLocation() != null) i.getLocation().getName();
            if (i.getReportedBy() != null) i.getReportedBy().getId();
            if (i.getTags() != null) i.getTags().size();
        });
        return issue;
    }

    public List<IssueReport> getByUser(UUID userId) { 
        return repo.findByReportedById(userId); 
    }

    public IssueReport updateStatus(UUID id, Status status) { 
        IssueReport issue = repo.findById(id).orElseThrow();
        Status oldStatus = issue.getStatus();
        issue.setStatus(status); 
        if (status == Status.RESOLVED) 
            issue.setDateResolved(Instant.now()); 
        
        IssueReport savedIssue = repo.save(issue);
        
        // System-generated notification: Notify reporting resident about status change
        if (oldStatus != status && savedIssue.getReportedBy() != null) {
            notifyResidentAboutStatusChange(savedIssue, oldStatus, status);
        }
        
        return savedIssue; 
    }

    /**
     * Notify the reporting resident when issue status changes
     */
    private void notifyResidentAboutStatusChange(IssueReport issue, Status oldStatus, Status newStatus) {
        try {
            User resident = issue.getReportedBy();
            if (resident == null) return;
            
            String statusMessage = getStatusChangeMessage(issue, oldStatus, newStatus);
            notificationService.createNotification(resident, issue, statusMessage);
        } catch (Exception e) {
            // Log error but don't fail status update if notification fails
            System.err.println("Error creating notification for status change: " + e.getMessage());
        }
    }

    /**
     * Generate appropriate message based on status change
     */
    private String getStatusChangeMessage(IssueReport issue, Status oldStatus, Status newStatus) {
        String issueTitle = issue.getTitle();
        
        switch (newStatus) {
            case IN_PROGRESS:
                return String.format("Your issue '%s' is now being processed", issueTitle);
            case RESOLVED:
                return String.format("Your issue '%s' has been resolved", issueTitle);
            case REPORTED:
                return String.format("Your issue '%s' status has been updated to Reported", issueTitle);
            default:
                return String.format("Your issue '%s' status has been updated from %s to %s", 
                    issueTitle, oldStatus, newStatus);
        }
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

    // Dashboard helper methods (without role filtering - for backward compatibility)
    public long count() {
        return repo.count();
    }

    public long countByStatus(String status) {
        try {
            Status s = Status.valueOf(status);
            return repo.countByStatus(s);
        } catch (Exception e) {
            return 0L;
        }
    }

    public List<IssueReport> findTop5ByOrderByDateReportedDesc() {
        return repo.findTop5ByOrderByDateReportedDesc();
    }

    public List<Map<String, Object>> countByCategory() {
        var list = repo.findAll();
        Map<String, Long> map = new java.util.HashMap<>();
        for (var i : list) {
            String cat = i.getCategory() == null ? "UNCATEGORIZED" : i.getCategory();
            map.put(cat, map.getOrDefault(cat, 0L) + 1);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (var e : map.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("category", e.getKey());
            m.put("count", e.getValue());
            out.add(m);
        }
        return out;
    }

    public List<Map<String, Object>> countByLocation() {
        var list = repo.findAll();
        Map<String, Long> map = new java.util.HashMap<>();
        for (var i : list) {
            String loc = i.getLocation() == null || i.getLocation().getName() == null ? "UNKNOWN" : i.getLocation().getName();
            map.put(loc, map.getOrDefault(loc, 0L) + 1);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (var e : map.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("location", e.getKey());
            m.put("count", e.getValue());
            out.add(m);
        }
        return out;
    }

    // Dashboard helper methods with role-based filtering
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public long count(User currentUser) {
        if (currentUser == null) {
            return 0L;
        }
        if (currentUser.getRole() == Role.ADMIN) {
            return repo.count();
        } else {
            return repo.findByReportedById(currentUser.getId()).size();
        }
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public long countByStatus(String status, User currentUser) {
        if (currentUser == null) {
            return 0L;
        }
        try {
            Status s = Status.valueOf(status);
            List<IssueReport> issues;
            if (currentUser.getRole() == Role.ADMIN) {
                issues = repo.findByStatus(s);
            } else {
                issues = repo.findByReportedById(currentUser.getId()).stream()
                    .filter(i -> i.getStatus() == s)
                    .toList();
            }
            return issues.size();
        } catch (Exception e) {
            return 0L;
        }
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<IssueReport> findTop5ByOrderByDateReportedDesc(User currentUser) {
        if (currentUser == null) {
            return List.of();
        }
        List<IssueReport> issues;
        if (currentUser.getRole() == Role.ADMIN) {
            issues = repo.findTop5ByOrderByDateReportedDesc();
        } else {
            issues = repo.findByReportedById(currentUser.getId()).stream()
                .sorted((a, b) -> b.getDateReported().compareTo(a.getDateReported()))
                .limit(5)
                .toList();
        }
        // Force load relationships before transaction closes
        issues.forEach(issue -> {
            if (issue.getLocation() != null) issue.getLocation().getName();
            if (issue.getReportedBy() != null) issue.getReportedBy().getId();
            if (issue.getTags() != null) issue.getTags().size();
        });
        return issues;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Map<String, Object>> countByCategory(User currentUser) {
        if (currentUser == null) {
            return List.of();
        }
        List<IssueReport> list;
        if (currentUser.getRole() == Role.ADMIN) {
            list = repo.findAll();
        } else {
            list = repo.findByReportedById(currentUser.getId());
        }
        Map<String, Long> map = new java.util.HashMap<>();
        for (var i : list) {
            String cat = i.getCategory() == null ? "UNCATEGORIZED" : i.getCategory();
            map.put(cat, map.getOrDefault(cat, 0L) + 1);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (var e : map.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("category", e.getKey());
            m.put("count", e.getValue());
            out.add(m);
        }
        return out;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Map<String, Object>> countByLocation(User currentUser) {
        if (currentUser == null) {
            return List.of();
        }
        List<IssueReport> list;
        if (currentUser.getRole() == Role.ADMIN) {
            list = repo.findAll();
        } else {
            list = repo.findByReportedById(currentUser.getId());
        }
        Map<String, Long> map = new java.util.HashMap<>();
        for (var i : list) {
            // Force load location relationship within transaction
            String loc = "UNKNOWN";
            if (i.getLocation() != null) {
                loc = i.getLocation().getName();
                if (loc == null) loc = "UNKNOWN";
            }
            map.put(loc, map.getOrDefault(loc, 0L) + 1);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (var e : map.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("location", e.getKey());
            m.put("count", e.getValue());
            out.add(m);
        }
        return out;
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

    /**
     * Search issues by query string (searches title, description, category)
     * With role-based filtering
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<IssueReport> search(String query, User currentUser) {
        if (currentUser == null) {
            return List.of();
        }
        
        String lowerQuery = query.toLowerCase();
        List<IssueReport> issuesToSearch;
        
        if (currentUser.getRole() == Role.ADMIN) {
            // Admin searches all issues
            issuesToSearch = repo.findAll();
        } else {
            // Resident searches only their own issues
            issuesToSearch = repo.findByReportedById(currentUser.getId());
        }
        
        List<IssueReport> results = issuesToSearch.stream()
            .filter(issue -> 
                (issue.getTitle() != null && issue.getTitle().toLowerCase().contains(lowerQuery)) ||
                (issue.getDescription() != null && issue.getDescription().toLowerCase().contains(lowerQuery)) ||
                (issue.getCategory() != null && issue.getCategory().toLowerCase().contains(lowerQuery)) ||
                (issue.getStatus() != null && issue.getStatus().toString().toLowerCase().contains(lowerQuery))
            )
            .toList();
        
        // Force load relationships before transaction closes
        results.forEach(issue -> {
            if (issue.getLocation() != null) issue.getLocation().getName();
            if (issue.getReportedBy() != null) issue.getReportedBy().getId();
            if (issue.getTags() != null) issue.getTags().size();
        });
        
        return results;
    }
}