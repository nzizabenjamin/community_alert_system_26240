package com.comunityalert.cas.repository;

import com.comunityalert.cas.enums.Status;
import com.comunityalert.cas.model.IssueReport;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;


public interface IssueRepository extends JpaRepository<IssueReport, UUID> {
List<IssueReport> findByReportedById(UUID userId);
List<IssueReport> findByStatus(Status status);
List<IssueReport> findByCategory(String category);
//Page<IssueReport> findAll(Pageable pageable);
}