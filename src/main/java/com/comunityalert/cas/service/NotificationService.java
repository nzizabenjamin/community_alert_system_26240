package com.comunityalert.cas.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.comunityalert.cas.enums.Channel;
import com.comunityalert.cas.enums.Role;
import com.comunityalert.cas.model.IssueReport;
import com.comunityalert.cas.model.Notification;
import com.comunityalert.cas.model.User;
import com.comunityalert.cas.repository.NotificationRepository;

@Service
public class NotificationService {
    
    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) { 
        this.repo = repo; 
    }

    /**
     * Create and send a notification (system-generated)
     * @param recipient The user who will receive the notification
     * @param issue The issue related to this notification (can be null)
     * @param message The notification message
     * @return The created notification
     */
    public Notification createNotification(User recipient, IssueReport issue, String message) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setIssue(issue);
        n.setMessage(message);
        n.setChannel(Channel.SYSTEM);
        n.setSentAt(Instant.now());
        n.setDelivered(true);
        n.setRead(false);
        return repo.save(n);
    }

    /**
     * Legacy method - kept for backward compatibility
     * @deprecated Use createNotification instead
     */
    @Deprecated
    public Notification send(User recipient, IssueReport issue, String message) {
        return createNotification(recipient, issue, message);
    }

    /**
     * Mark a notification as read
     */
    public Notification markAsRead(UUID notificationId) {
        Notification notification = repo.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return repo.save(notification);
    }

    public List<Notification> getByRecipient(UUID userId) {
        return repo.findByRecipientId(userId);
    }

    public List<Notification> getByIssue(UUID issueId) {
        return repo.findByIssueId(issueId);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Notification> getAll(Pageable pageable) {
        Page<Notification> pageData = repo.findAll(pageable);
        // Force load relationships before transaction closes
        pageData.getContent().forEach(notif -> {
            try {
                // Initialize recipient proxy
                if (notif.getRecipient() != null) {
                    notif.getRecipient().getId();
                    notif.getRecipient().getEmail();
                }
                // Initialize issue proxy
                if (notif.getIssue() != null) {
                    notif.getIssue().getId();
                    notif.getIssue().getTitle();
                }
            } catch (Exception e) {
                System.err.println("DEBUG NotificationService: Error loading relationships: " + e.getMessage());
            }
        });
        return pageData;
    }

    /**
     * Get notifications with role-based filtering
     * RESIDENT users only see their own notifications
     * ADMIN users see all notifications
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Notification> getAll(Pageable pageable, User currentUser) {
        if (currentUser == null) {
            return Page.empty(pageable);
        }
        
        Page<Notification> pageData;
        if (currentUser.getRole() == Role.ADMIN) {
            // Admin sees all notifications
            pageData = repo.findAll(pageable);
        } else {
            // Resident sees only their own notifications
            pageData = repo.findByRecipientId(currentUser.getId(), pageable);
        }
        
        // Force load relationships before transaction closes
        pageData.getContent().forEach(notif -> {
            try {
                // Initialize recipient proxy
                if (notif.getRecipient() != null) {
                    notif.getRecipient().getId();
                    notif.getRecipient().getEmail();
                }
                // Initialize issue proxy
                if (notif.getIssue() != null) {
                    notif.getIssue().getId();
                    notif.getIssue().getTitle();
                }
            } catch (Exception e) {
                System.err.println("DEBUG NotificationService: Error loading relationships: " + e.getMessage());
            }
        });
        
        return pageData;
    }
}
