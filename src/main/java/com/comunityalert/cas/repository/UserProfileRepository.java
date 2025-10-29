package com.comunityalert.cas.repository;

import com.comunityalert.cas.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    
    // Find profile by user ID
    Optional<UserProfile> findByUserId(UUID userId);
    
    // Check if user has a profile
    boolean existsByUserId(UUID userId);
    
    // Find profile by national ID
    Optional<UserProfile> findByNationalId(String nationalId);
    
    // Check if national ID exists
    boolean existsByNationalId(String nationalId);
    
    // Custom query: Find profiles with bio
    @Query("SELECT p FROM UserProfile p WHERE p.bio IS NOT NULL AND p.bio <> ''")
    java.util.List<UserProfile> findProfilesWithBio();
}