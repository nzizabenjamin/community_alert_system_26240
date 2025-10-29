package com.comunityalert.cas.controller;

import com.comunityalert.cas.model.Tag;
import com.comunityalert.cas.service.TagService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    
    private final TagService service;

    public TagController(TagService service) {
        this.service = service;
    }

    /**
     * Create a new tag
     * POST /api/tags
     */
    @PostMapping
    public ResponseEntity<Tag> create(@RequestBody Tag tag) {
        return ResponseEntity.ok(service.create(tag));
    }

    /**
     * Get all tags
     * GET /api/tags
     */
    @GetMapping
    public ResponseEntity<List<Tag>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /**
     * Get tag by ID
     * GET /api/tags/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tag> getById(@PathVariable UUID id) {
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search tags by name
     * GET /api/tags/search?name=urgent
     */
    @GetMapping("/search")
    public ResponseEntity<List<Tag>> search(@RequestParam String name) {
        return ResponseEntity.ok(service.searchByName(name));
    }

    /**
     * Get all used tags (tags that are assigned to at least one issue)
     * GET /api/tags/used
     */
    @GetMapping("/used")
    public ResponseEntity<List<Tag>> getUsed() {
        return ResponseEntity.ok(service.getUsedTags());
    }

    /**
     * Get all unused tags
     * GET /api/tags/unused
     */
    @GetMapping("/unused")
    public ResponseEntity<List<Tag>> getUnused() {
        return ResponseEntity.ok(service.getUnusedTags());
    }

    /**
     * Update a tag
     * PUT /api/tags/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tag> update(@PathVariable UUID id, @RequestBody Tag payload) {
        return ResponseEntity.ok(service.update(id, payload));
    }

    /**
     * Delete a tag
     * DELETE /api/tags/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}