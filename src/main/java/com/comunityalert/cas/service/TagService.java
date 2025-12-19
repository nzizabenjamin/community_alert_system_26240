package com.comunityalert.cas.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.comunityalert.cas.model.Tag;
import com.comunityalert.cas.repository.TagRepository;

@Service
public class TagService {
    
    private final TagRepository repo;

    public TagService(TagRepository repo) {
        this.repo = repo;
    }

    /**
     * Create a new tag
     */
    public Tag create(Tag tag) {
        // Check if tag with same name already exists
        if (repo.existsByName(tag.getName())) {
            throw new RuntimeException("Tag with name '" + tag.getName() + "' already exists");
        }
        return repo.save(tag);
    }

    /**
     * Get all tags
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Tag> getAll() {
        return repo.findAll();
    }

    /**
     * Get all tags with pagination
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Tag> getAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    /**
     * Get all active tags (for residents to select from)
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Tag> getActiveTags() {
        return repo.findByActiveTrue();
    }

    /**
     * Get all active tags with pagination
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Tag> getActiveTags(Pageable pageable) {
        return repo.findByActiveTrue(pageable);
    }

    /**
     * Get tag by ID
     */
    public Optional<Tag> getById(UUID id) {
        return repo.findById(id);
    }

    /**
     * Get tag by name
     */
    public Optional<Tag> getByName(String name) {
        return repo.findByName(name);
    }

    /**
     * Search tags by name (partial match)
     */
    public List<Tag> searchByName(String name) {
        return repo.findByNameContainingIgnoreCase(name);
    }

    /**
     * Get all tags that are used in at least one issue
     */
    public List<Tag> getUsedTags() {
        return repo.findUsedTags();
    }

    /**
     * Get all tags that are not used in any issue
     */
    public List<Tag> getUnusedTags() {
        return repo.findUnusedTags();
    }

    /**
     * Update a tag (rename, change description, or activate/deactivate)
     */
    public Tag update(UUID id, Tag payload) {
        Tag existing = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Tag not found"));
        
        // Check if new name conflicts with another tag
        if (!existing.getName().equals(payload.getName()) && 
            repo.existsByName(payload.getName())) {
            throw new RuntimeException("Tag with name '" + payload.getName() + "' already exists");
        }
        
        existing.setName(payload.getName());
        if (payload.getDescription() != null) {
            existing.setDescription(payload.getDescription());
        }
        existing.setActive(payload.isActive());
        return repo.save(existing);
    }

    /**
     * Deactivate a tag (soft delete - doesn't remove from issues)
     */
    public Tag deactivate(UUID id) {
        Tag tag = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Tag not found"));
        tag.setActive(false);
        return repo.save(tag);
    }

    /**
     * Activate a tag
     */
    public Tag activate(UUID id) {
        Tag tag = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Tag not found"));
        tag.setActive(true);
        return repo.save(tag);
    }

    /**
     * Validate that tag IDs exist and are active (for residents selecting tags)
     */
    public void validateTagIds(List<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return; // No tags selected is valid
        }
        
        for (UUID tagId : tagIds) {
            Tag tag = repo.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag with ID " + tagId + " not found"));
            
            if (!tag.isActive()) {
                throw new RuntimeException("Tag '" + tag.getName() + "' is not active and cannot be selected");
            }
        }
    }

    /**
     * Delete a tag
     */
    public void delete(UUID id) {
        Tag tag = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Tag not found"));
        
        // Remove tag from all issues before deleting
        tag.getIssues().forEach(issue -> issue.getTags().remove(tag));
        
        repo.deleteById(id);
    }

    /**
     * Get or create tag by name (useful for bulk operations)
     */
    public Tag getOrCreate(String name) {
        return repo.findByName(name)
            .orElseGet(() -> {
                Tag newTag = new Tag();
                newTag.setName(name);
                return repo.save(newTag);
            });
    }
}