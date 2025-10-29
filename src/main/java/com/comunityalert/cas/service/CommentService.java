package com.comunityalert.cas.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.comunityalert.cas.model.Comment;
import com.comunityalert.cas.model.User;
import com.comunityalert.cas.repository.CommentRepository;
import com.comunityalert.cas.repository.UserRepository;

@Service
public class CommentService {
    
    private final CommentRepository repo;
    private final UserRepository userRepo;

    public CommentService(CommentRepository repo, UserRepository userRepo) { 
        this.repo = repo;
        this.userRepo = userRepo;
    }

    public Comment addComment(Comment c) {
        // Fetch and set the actual User entity if ID is provided
        if (c.getCreatedBy() != null && c.getCreatedBy().getId() != null) {
            User user = userRepo.findById(c.getCreatedBy().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            c.setCreatedBy(user);
        }
        
        return repo.save(c); 
    }

    /**
     * Add comment from DTO (cleaner approach)
     */
    public Comment addCommentFromDTO(com.comunityalert.cas.dto.CreateCommentDTO dto) {
        Comment comment = new Comment();
        comment.setMessage(dto.getMessage());
        
        // Fetch user
        User user = userRepo.findById(dto.getCreatedById())
            .orElseThrow(() -> new RuntimeException("User not found"));
        comment.setCreatedBy(user);
        
        return repo.save(comment);
    }

    public List<Comment> getByIssue(UUID issueId) { 
        return repo.findByIssueId(issueId); 
    }

    public void delete(UUID id) { 
        repo.deleteById(id); 
    }
}