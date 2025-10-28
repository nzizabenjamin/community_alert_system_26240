package com.comunityalert.cas.controller;

import com.comunityalert.cas.model.Comment;
import com.comunityalert.cas.model.IssueReport;

import com.comunityalert.cas.service.CommentService;
import com.comunityalert.cas.service.IssueService;
import com.comunityalert.cas.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/issues/{issueId}/comments")
public class CommentController {
    private final CommentService commentService;

    private final IssueService issueService;

    private final UserService userService;


public CommentController(CommentService commentService, IssueService issueService, UserService userService) {

    this.commentService = commentService; 
    this.issueService = issueService; 
    this.userService = userService;
}


@PostMapping
public ResponseEntity<Comment> add(@PathVariable UUID issueId, @RequestBody Comment c) {
    IssueReport issue = issueService.getById(issueId).orElseThrow();
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