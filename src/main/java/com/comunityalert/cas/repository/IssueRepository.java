package com.comunityalert.cas.repository;

import com.comunityalert.cas.enums.Status;
import com.comunityalert.cas.model.IssueReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<IssueReport, UUID> {
    // Use explicit queries for relationship navigation
    @Query("SELECT i FROM IssueReport i WHERE i.reportedBy.id = :userId")
    List<IssueReport> findByReportedById(@Param("userId") UUID userId);
    
    @Query("SELECT i FROM IssueReport i WHERE i.reportedBy.id = :userId")
    Page<IssueReport> findByReportedById(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT i FROM IssueReport i WHERE i.status = :status")
    List<IssueReport> findByStatus(@Param("status") Status status);
    
    @Query("SELECT i FROM IssueReport i WHERE i.category = :category")
    List<IssueReport> findByCategory(@Param("category") String category);

    @Query("SELECT COUNT(i) FROM IssueReport i WHERE i.status = :status")
    long countByStatus(@Param("status") Status status);

    @Query(value = "SELECT i.* FROM issues i ORDER BY i.date_reported DESC LIMIT 5", nativeQuery = true)
    List<IssueReport> findTop5ByOrderByDateReportedDesc();
}