package com.comunityalert.cas.controller;

import com.comunityalert.cas.model.IssueReport;
import com.comunityalert.cas.model.Notification;
import com.comunityalert.cas.model.User;
import com.comunityalert.cas.service.IssueService;
import com.comunityalert.cas.service.NotificationService;
import com.comunityalert.cas.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private final NotificationService service;
    private final UserService userService;
    private final IssueService issueService;

    public NotificationController(NotificationService service, UserService userService, IssueService issueService) {
        this.service = service; 
        this.userService = userService; 
        this.issueService = issueService;
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
}