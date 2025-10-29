package com.comunityalert.cas.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

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
    public List<Tag> getAll() {
        return repo.findAll();
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
     * Update a tag
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
        existing.setDescription(payload.getDescription());
        return repo.save(existing);
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