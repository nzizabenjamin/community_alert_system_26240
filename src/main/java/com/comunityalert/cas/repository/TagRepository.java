package com.comunityalert.cas.repository;

import com.comunityalert.cas.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    
    // Find tag by exact name
    Optional<Tag> findByName(String name);
    
    // Check if tag exists by name
    boolean existsByName(String name);
    
    // Find tags by name containing (case insensitive)
    List<Tag> findByNameContainingIgnoreCase(String name);
    
    // Custom query: Get all tags used in at least one issue
    @Query("SELECT DISTINCT t FROM Tag t WHERE SIZE(t.issues) > 0")
    List<Tag> findUsedTags();
    
    // Custom query: Get unused tags
    @Query("SELECT t FROM Tag t WHERE SIZE(t.issues) = 0")
    List<Tag> findUnusedTags();
}