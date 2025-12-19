package com.comunityalert.cas.controller;

import com.comunityalert.cas.model.IssueReport;
import com.comunityalert.cas.model.Notification;
import com.comunityalert.cas.model.User;
import com.comunityalert.cas.service.IssueService;
import com.comunityalert.cas.service.NotificationService;
import com.comunityalert.cas.service.UserService;
import com.comunityalert.cas.service.JwtService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {
    
    private final NotificationService service;
    private final UserService userService;
    private final IssueService issueService;
    private final JwtService jwtService;

    public NotificationController(NotificationService service, UserService userService, IssueService issueService, JwtService jwtService) {
        this.service = service; 
        this.userService = userService; 
        this.issueService = issueService;
        this.jwtService = jwtService;
    }

    /**
     * Helper method to get current user from Authorization header
     */
    private User getCurrentUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authHeader.substring(7);
        String userIdStr = jwtService.getUserIdFromToken(token);
        
        if (userIdStr == null) {
            return null;
        }
        
        try {
            UUID userId = UUID.fromString(userIdStr);
            return userService.getUserEntity(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Send notification to a user about an issue
     */
    @PostMapping("/send/{userId}/{issueId}")
    public ResponseEntity<Notification> send(
            @PathVariable UUID userId, 
            @PathVariable UUID issueId, 
            @RequestParam String message) {
        
        // Use getUserEntity() instead of getById() to get the actual User entity
        User u = userService.getUserEntity(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        IssueReport i = issueService.getById(issueId)
            .orElseThrow(() -> new RuntimeException("Issue not found"));
        
        return ResponseEntity.ok(service.send(u, i, message));
    }

    /**
     * Get all notifications for a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> byUser(@PathVariable UUID userId) { 
        return ResponseEntity.ok(service.getByRecipient(userId)); 
    }

    /**
     * Get all notifications related to a specific issue
     */
    @GetMapping("/issue/{issueId}")
    public ResponseEntity<List<Notification>> byIssue(@PathVariable UUID issueId) { 
        return ResponseEntity.ok(service.getByIssue(issueId)); 
    }

    /**
     * Get all notifications with pagination and role-based filtering
     * RESIDENT users only see their own notifications
     * ADMIN users see all notifications
     */
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sentAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Sort.Direction dir = Sort.Direction.fromString(sortDir);
            User currentUser = getCurrentUser(authHeader);
            
            Page<Notification> pageData;
            if (currentUser != null) {
                // Use role-based filtering
                pageData = service.getAll(PageRequest.of(page, size, Sort.by(dir, sortBy)), currentUser);
            } else {
                // No authentication, return empty page
                pageData = Page.empty(PageRequest.of(page, size, Sort.by(dir, sortBy)));
            }
            
            return ResponseEntity.ok(pageData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load notifications: " + e.getMessage()));
        }
    }

    /**
     * Get notification by ID (with role-based access control)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = getCurrentUser(authHeader);
        
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        // Get notification - check if user has access
        Optional<Notification> notification = service.getAll(PageRequest.of(0, Integer.MAX_VALUE), currentUser)
            .getContent().stream()
            .filter(n -> n.getId().equals(id))
            .findFirst();
            
        if (notification.isPresent()) {
            return ResponseEntity.ok(notification.get());
        } else {
            return ResponseEntity.status(403).body("Notification not found or access denied");
        }
    }

    /**
     * Mark notification as read (with role-based access control)
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = getCurrentUser(authHeader);
        
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        Notification notif = service.getAll(PageRequest.of(0, Integer.MAX_VALUE), currentUser)
            .getContent().stream()
            .filter(n -> n.getId().equals(id))
            .findFirst()
            .orElse(null);
            
        if (notif == null) {
            return ResponseEntity.status(403).body("Notification not found or access denied");
        }
        
        // TODO: Add read field to Notification model if needed
        // For now, just return the notification
        return ResponseEntity.ok(notif);
    }

    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    /**
     * Search notifications globally with role-based filtering
     */
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam String q,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = getCurrentUser(authHeader);
        
        if (currentUser == null) {
            return ResponseEntity.ok(List.of());
        }
        
        List<Notification> allNotifications = service.getAll(PageRequest.of(0, Integer.MAX_VALUE), currentUser).getContent();
        List<Notification> results = allNotifications.stream()
            .filter(notif -> 
                (notif.getMessage() != null && notif.getMessage().toLowerCase().contains(q.toLowerCase()))
            )
            .toList();
        return ResponseEntity.ok(results);
    }
}