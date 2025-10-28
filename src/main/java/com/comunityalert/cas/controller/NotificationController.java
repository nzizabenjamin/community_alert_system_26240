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


@PostMapping("/send/{userId}/{issueId}")
public ResponseEntity<Notification> send(@PathVariable UUID userId, @PathVariable UUID issueId, @RequestParam String message) {
    
    User u = userService.getById(userId).orElseThrow();
    IssueReport i = issueService.getById(issueId).orElseThrow();
    return ResponseEntity.ok(service.send(u, i, message));
}


@GetMapping("/user/{userId}")
public ResponseEntity<List<Notification>> byUser(@PathVariable UUID userId) { 
    
    return ResponseEntity.ok(service.getByRecipient(userId)); 

}


@GetMapping("/issue/{issueId}")
public ResponseEntity<List<Notification>> byIssue(@PathVariable UUID issueId) { 
    
    return ResponseEntity.ok(service.getByIssue(issueId)); 

}
}