package com.comunityalert.cas.controller;

import com.comunityalert.cas.dto.CreateIssueDTO;
import com.comunityalert.cas.model.IssueReport;
import com.comunityalert.cas.model.Tag;
import com.comunityalert.cas.enums.Status;
import com.comunityalert.cas.service.IssueService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/issues")
public class IssueController {
    private final IssueService service;
    
    public IssueController(IssueService service) { 
        this.service = service; 
    }

    /**
     * Create issue using DTO (cleaner approach)
     */
    @PostMapping
    public ResponseEntity<IssueReport> create(@RequestBody CreateIssueDTO dto) { 
        return ResponseEntity.ok(service.createFromDTO(dto)); 
    }

    /**
     * Alternative: Create issue using entity (for backward compatibility)
     */
    @PostMapping("/entity")
    public ResponseEntity<IssueReport> createFromEntity(@RequestBody IssueReport i) { 
        return ResponseEntity.ok(service.create(i)); 
    }

    @GetMapping
    public ResponseEntity<List<IssueReport>> getAll(
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        Page<IssueReport> p = service.getAll(PageRequest.of(page, size));
        return ResponseEntity.ok(p.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueReport> getById(@PathVariable UUID id) { 
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build()); 
    }

    @PutMapping("/{id}")
    public ResponseEntity<IssueReport> update(@PathVariable UUID id, @RequestBody IssueReport payload) { 
        return ResponseEntity.ok(service.update(id, payload)); 
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<IssueReport> updateStatus(@PathVariable UUID id, @RequestParam Status status) { 
        return ResponseEntity.ok(service.updateStatus(id, status)); 
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) { 
        service.delete(id); 
        return ResponseEntity.noContent().build(); 
    }

    // ========== TAG OPERATIONS ==========

    /**
     * Add a tag to an issue
     * POST /api/issues/{issueId}/tags/{tagId}
     */
    @PostMapping("/{issueId}/tags/{tagId}")
    public ResponseEntity<IssueReport> addTag(
            @PathVariable UUID issueId, 
            @PathVariable UUID tagId) {
        return ResponseEntity.ok(service.addTag(issueId, tagId));
    }

    /**
     * Remove a tag from an issue
     * DELETE /api/issues/{issueId}/tags/{tagId}
     */
    @DeleteMapping("/{issueId}/tags/{tagId}")
    public ResponseEntity<IssueReport> removeTag(
            @PathVariable UUID issueId, 
            @PathVariable UUID tagId) {
        return ResponseEntity.ok(service.removeTag(issueId, tagId));
    }

    /**
     * Get all tags for an issue
     * GET /api/issues/{issueId}/tags
     */
    @GetMapping("/{issueId}/tags")
    public ResponseEntity<Set<Tag>> getIssueTags(@PathVariable UUID issueId) {
        return ResponseEntity.ok(service.getIssueTags(issueId));
    }
}