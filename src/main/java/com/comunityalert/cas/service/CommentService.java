package com.comunityalert.cas.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.comunityalert.cas.model.Comment;
import com.comunityalert.cas.repository.CommentRepository;

@Service
public class CommentService {
    
    private final CommentRepository repo;

    public CommentService(CommentRepository repo) { 
        this.repo = repo; 
    }

    public Comment addComment(Comment c) { 
        return repo.save(c); 
    }

    public List<Comment> getByIssue(UUID issueId) { 
        return repo.findByIssueId(issueId); 
    }

    public void delete(UUID id) { 
        repo.deleteById(id); 
    }
}
