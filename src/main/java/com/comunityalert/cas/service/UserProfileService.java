package com.comunityalert.cas.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.comunityalert.cas.model.User;
import com.comunityalert.cas.model.UserProfile;
import com.comunityalert.cas.repository.UserProfileRepository;
import com.comunityalert.cas.repository.UserRepository;

@Service
public class UserProfileService {
    
    private final UserProfileRepository profileRepo;
    private final UserRepository userRepo;

    public UserProfileService(UserProfileRepository profileRepo, UserRepository userRepo) {
        this.profileRepo = profileRepo;
        this.userRepo = userRepo;
    }

    /**
     * Create or update a profile for a user
     */
    public UserProfile createOrUpdate(UUID userId, UserProfile profile) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user already has a profile
        Optional<UserProfile> existingProfile = profileRepo.findByUserId(userId);
        
        if (existingProfile.isPresent()) {
            // Update existing profile
            UserProfile existing = existingProfile.get();
            existing.setBio(profile.getBio());
            existing.setProfilePictureUrl(profile.getProfilePictureUrl());
            existing.setAddress(profile.getAddress());
            existing.setNationalId(profile.getNationalId());
            existing.setEmergencyContact(profile.getEmergencyContact());
            return profileRepo.save(existing);
        } else {
            // Create new profile
            profile.setUser(user);
            return profileRepo.save(profile);
        }
    }

    /**
     * Get all profiles
     */
    public List<UserProfile> getAll() {
        return profileRepo.findAll();
    }

    /**
     * Get profile by ID
     */
    public Optional<UserProfile> getById(UUID id) {
        return profileRepo.findById(id);
    }

    /**
     * Get profile by user ID
     */
    public Optional<UserProfile> getByUserId(UUID userId) {
        return profileRepo.findByUserId(userId);
    }

    /**
     * Get profile by national ID
     */
    public Optional<UserProfile> getByNationalId(String nationalId) {
        return profileRepo.findByNationalId(nationalId);
    }

    /**
     * Update profile
     */
    public UserProfile update(UUID profileId, UserProfile payload) {
        UserProfile existing = profileRepo.findById(profileId)
            .orElseThrow(() -> new RuntimeException("Profile not found"));
        
        // Check if national ID is being changed and if it conflicts
        if (payload.getNationalId() != null && 
            !payload.getNationalId().equals(existing.getNationalId()) &&
            profileRepo.existsByNationalId(payload.getNationalId())) {
            throw new RuntimeException("National ID already in use");
        }
        
        existing.setBio(payload.getBio());
        existing.setProfilePictureUrl(payload.getProfilePictureUrl());
        existing.setAddress(payload.getAddress());
        existing.setNationalId(payload.getNationalId());
        existing.setEmergencyContact(payload.getEmergencyContact());
        
        return profileRepo.save(existing);
    }

    /**
     * Delete profile
     */
    public void delete(UUID profileId) {
        profileRepo.deleteById(profileId);
    }

    /**
     * Delete profile by user ID
     */
    public void deleteByUserId(UUID userId) {
        profileRepo.findByUserId(userId)
            .ifPresent(profile -> profileRepo.deleteById(profile.getId()));
    }

    /**
     * Check if user has a profile
     */
    public boolean userHasProfile(UUID userId) {
        return profileRepo.existsByUserId(userId);
    }

    /**
     * Get all profiles that have bio
     */
    public List<UserProfile> getProfilesWithBio() {
        return profileRepo.findProfilesWithBio();
    }
}