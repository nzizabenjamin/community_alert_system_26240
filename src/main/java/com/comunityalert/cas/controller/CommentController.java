package com.comunityalert.cas.controller;

import com.comunityalert.cas.dto.CreateCommentDTO;
import com.comunityalert.cas.model.Comment;
import com.comunityalert.cas.model.IssueReport;
import com.comunityalert.cas.service.CommentService;
import com.comunityalert.cas.service.IssueService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issues/{issueId}/comments")
public class CommentController {
    private final CommentService commentService;
    private final IssueService issueService;

    public CommentController(CommentService commentService, IssueService issueService) {
        this.commentService = commentService; 
        this.issueService = issueService;
    }

    /**
     * Add comment using DTO (cleaner approach)
     */
    @PostMapping
    public ResponseEntity<Comment> add(@PathVariable UUID issueId, @RequestBody CreateCommentDTO dto) {
        IssueReport issue = issueService.getById(issueId)
            .orElseThrow(() -> new RuntimeException("Issue not found"));
        
        Comment comment = commentService.addCommentFromDTO(dto);
        comment.setIssue(issue);
        
        return ResponseEntity.ok(commentService.addComment(comment));
    }

    /**
     * Alternative: Add comment using entity (for backward compatibility)
     */
    @PostMapping("/entity")
    public ResponseEntity<Comment> addFromEntity(@PathVariable UUID issueId, @RequestBody Comment c) {
        IssueReport issue = issueService.getById(issueId)
            .orElseThrow(() -> new RuntimeException("Issue not found"));
        c.setIssue(issue);
        return ResponseEntity.ok(commentService.addComment(c));
    }

    @GetMapping
    public ResponseEntity<List<Comment>> getAll(@PathVariable UUID issueId) { 
        return ResponseEntity.ok(commentService.getByIssue(issueId)); 
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID issueId, @PathVariable UUID id) { 
        commentService.delete(id); 
        return ResponseEntity.noContent().build(); 
    }
}