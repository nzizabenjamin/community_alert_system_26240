package com.comunityalert.cas.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.comunityalert.cas.enums.Channel;
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

    public Notification send(User recipient, IssueReport issue, String message) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setIssue(issue);
        n.setMessage(message);
        n.setChannel(Channel.SYSTEM);
        n.setSentAt(Instant.now());
        n.setDelivered(true);
        return repo.save(n);
    }

    public List<Notification> getByRecipient(UUID userId) {
        return repo.findByRecipientId(userId);
    }

    public List<Notification> getByIssue(UUID issueId) {
        return repo.findByIssueId(issueId);
    }
}
