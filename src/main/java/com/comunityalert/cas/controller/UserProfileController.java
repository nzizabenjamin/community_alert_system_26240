package com.comunityalert.cas.controller;

import com.comunityalert.cas.model.UserProfile;
import com.comunityalert.cas.service.UserProfileService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {
    
    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    /**
     * Create or update profile for a user
     * POST /api/profiles/user/{userId}
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<UserProfile> createOrUpdate(
            @PathVariable UUID userId, 
            @RequestBody UserProfile profile) {
        return ResponseEntity.ok(service.createOrUpdate(userId, profile));
    }

    /**
     * Get all profiles
     * GET /api/profiles
     */
    @GetMapping
    public ResponseEntity<List<UserProfile>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /**
     * Get profile by profile ID
     * GET /api/profiles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getById(@PathVariable UUID id) {
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get profile by user ID
     * GET /api/profiles/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfile> getByUserId(@PathVariable UUID userId) {
        return service.getByUserId(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get profile by national ID
     * GET /api/profiles/national/{nationalId}
     */
    @GetMapping("/national/{nationalId}")
    public ResponseEntity<UserProfile> getByNationalId(@PathVariable String nationalId) {
        return service.getByNationalId(nationalId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Check if user has a profile
     * GET /api/profiles/user/{userId}/exists
     */
    @GetMapping("/user/{userId}/exists")
    public ResponseEntity<Boolean> userHasProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.userHasProfile(userId));
    }

    /**
     * Get profiles with bio
     * GET /api/profiles/with-bio
     */
    @GetMapping("/with-bio")
    public ResponseEntity<List<UserProfile>> getProfilesWithBio() {
        return ResponseEntity.ok(service.getProfilesWithBio());
    }

    /**
     * Update profile
     * PUT /api/profiles/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> update(
            @PathVariable UUID id, 
            @RequestBody UserProfile payload) {
        return ResponseEntity.ok(service.update(id, payload));
    }

    /**
     * Delete profile by profile ID
     * DELETE /api/profiles/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete profile by user ID
     * DELETE /api/profiles/user/{userId}
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteByUserId(@PathVariable UUID userId) {
        service.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}