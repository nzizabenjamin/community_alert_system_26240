package com.comunityalert.cas.repository;

import com.comunityalert.cas.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    // Use explicit @Query for all relationship queries to avoid JPA naming issues
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId")
    List<Notification> findByRecipient_Id(@Param("userId") UUID userId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId")
    Page<Notification> findByRecipient_Id(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.issue.id = :issueId")
    List<Notification> findByIssue_Id(@Param("issueId") UUID issueId);
    
    // Legacy methods for backward compatibility
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId")
    List<Notification> findByRecipientId(@Param("userId") UUID userId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId")
    Page<Notification> findByRecipientId(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.issue.id = :issueId")
    List<Notification> findByIssueId(@Param("issueId") UUID issueId);
}