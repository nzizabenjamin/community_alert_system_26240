package com.comunityalert.cas.repository;

import com.comunityalert.cas.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;


public interface CommentRepository extends JpaRepository<Comment, UUID> {
List<Comment> findByIssueId(UUID issueId);
}