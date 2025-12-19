package com.comunityalert.cas.controller;

import com.comunityalert.cas.model.Tag;
import com.comunityalert.cas.model.User;
import com.comunityalert.cas.enums.Role;
import com.comunityalert.cas.service.TagService;
import com.comunityalert.cas.service.JwtService;
import com.comunityalert.cas.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "http://localhost:5173")
public class TagController {
    
    private final TagService service;
    private final JwtService jwtService;
    private final UserService userService;

    public TagController(TagService service, JwtService jwtService, UserService userService) {
        this.service = service;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    /**
     * Helper method to get current user from Authorization header
     */
    private User getCurrentUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authHeader.substring(7);
        String userIdStr = jwtService.getUserIdFromToken(token);
        
        if (userIdStr == null) {
            return null;
        }
        
        try {
            UUID userId = UUID.fromString(userIdStr);
            return userService.getUserEntity(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if current user is ADMIN
     */
    private boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    /**
     * Create a new tag (ADMIN only)
     * POST /api/tags
     */
    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody Tag tag,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = getCurrentUser(authHeader);
        
        if (!isAdmin(currentUser)) {
            return ResponseEntity.status(403).body("Only administrators can create tags");
        }
        
        return ResponseEntity.ok(service.create(tag));
    }

    /**
     * Get all tags with pagination
     * ADMIN: sees all tags (active and inactive)
     * RESIDENT: sees only active tags (for selection)
     * GET /api/tags
     */
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Sort.Direction dir = Sort.Direction.fromString(sortDir);
            User currentUser = getCurrentUser(authHeader);
            
            Page<Tag> pageData;
            if (isAdmin(currentUser)) {
                // Admin sees all tags
                pageData = service.getAll(PageRequest.of(page, size, Sort.by(dir, sortBy)));
            } else {
                // Resident sees only active tags
                pageData = service.getActiveTags(PageRequest.of(page, size, Sort.by(dir, sortBy)));
            }
            
            return ResponseEntity.ok(pageData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load tags: " + e.getMessage()));
        }
    }

    /**
     * Get all active tags (for residents to select from)
     * GET /api/tags/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<Tag>> getActiveTags() {
        return ResponseEntity.ok(service.getActiveTags());
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
     * Search tags by name (supports both 'name' and 'q' parameters for compatibility)
     * GET /api/tags/search?q=urgent or /api/tags/search?name=urgent
     */
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String name) {
        String searchTerm = q != null ? q : (name != null ? name : "");
        if (searchTerm.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(service.searchByName(searchTerm));
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
     * Update a tag (rename, change description, activate/deactivate) - ADMIN only
     * PUT /api/tags/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable UUID id,
            @RequestBody Tag payload,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = getCurrentUser(authHeader);
        
        if (!isAdmin(currentUser)) {
            return ResponseEntity.status(403).body("Only administrators can update tags");
        }
        
        return ResponseEntity.ok(service.update(id, payload));
    }

    /**
     * Deactivate a tag (ADMIN only) - soft delete
     * PUT /api/tags/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = getCurrentUser(authHeader);
        
        if (!isAdmin(currentUser)) {
            return ResponseEntity.status(403).body("Only administrators can deactivate tags");
        }
        
        return ResponseEntity.ok(service.deactivate(id));
    }

    /**
     * Activate a tag (ADMIN only)
     * PUT /api/tags/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activate(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = getCurrentUser(authHeader);
        
        if (!isAdmin(currentUser)) {
            return ResponseEntity.status(403).body("Only administrators can activate tags");
        }
        
        return ResponseEntity.ok(service.activate(id));
    }

    /**
     * Delete a tag permanently (ADMIN only) - removes from all issues
     * DELETE /api/tags/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = getCurrentUser(authHeader);
        
        if (!isAdmin(currentUser)) {
            return ResponseEntity.status(403).body("Only administrators can delete tags");
        }
        
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}