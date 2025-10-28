package com.comunityalert.cas.repository;

import com.comunityalert.cas.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;


public interface NotificationRepository extends JpaRepository<Notification, UUID> {
List<Notification> findByRecipientId(UUID userId);
List<Notification> findByIssueId(UUID issueId);
}