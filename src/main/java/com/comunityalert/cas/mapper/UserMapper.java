package com.comunityalert.cas.mapper;

import org.springframework.stereotype.Component;
import com.comunityalert.cas.dto.CreateUserDTO;
import com.comunityalert.cas.dto.UserDTO;
import com.comunityalert.cas.model.Location;
import com.comunityalert.cas.model.User;
import com.comunityalert.cas.repository.LocationRepository;

@Component
public class UserMapper {
    
    private final LocationRepository locationRepo;

    public UserMapper(LocationRepository locationRepo) {
        this.locationRepo = locationRepo;
    }

    /**
     * Convert User entity to UserDTO (for responses)
     * Password is NOT included for security
     */
    public UserDTO toDTO(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        // Use fullName if available, otherwise extract name from email or use email
        String fullName = user.getFullName();
        if (fullName == null || fullName.trim().isEmpty()) {
            // Extract name from email (part before @) or use email as fallback
            String email = user.getEmail();
            if (email != null && email.contains("@")) {
                fullName = email.substring(0, email.indexOf("@"));
                // Capitalize first letter
                if (fullName.length() > 0) {
                    fullName = fullName.substring(0, 1).toUpperCase() + fullName.substring(1);
                }
            } else {
                fullName = email != null ? email : "Unknown User";
            }
        }
        dto.setFullName(fullName);
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        
        // ✅ CRITICAL: Map role - handle null case (default to RESIDENT)
        if (user.getRole() != null) {
            dto.setRole(user.getRole());
        } else {
            dto.setRole(com.comunityalert.cas.enums.Role.RESIDENT);  // Default to RESIDENT if null
        }
        
        dto.setCreatedAt(user.getCreatedAt());
        
        // ✅ Always set locationId if location exists (required by frontend)
        if (user.getLocation() != null) {
            dto.setLocationId(user.getLocation().getId());
            dto.setLocationName(user.getLocation().getName());
        } else {
            // Set to null explicitly if no location (frontend expects this field)
            dto.setLocationId(null);
            dto.setLocationName(null);
        }
        
        return dto;
    }

    /**
     * Convert CreateUserDTO to User entity (for creation)
     * NOTE: Role is set by UserService - this mapper should not override it
     */
    public User toEntity(CreateUserDTO dto) {
        if (dto == null) return null;

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());  // Will be hashed in service
        user.setPhoneNumber(dto.getPhoneNumber());
        // ✅ Role is set by UserService.create() - always RESIDENT for signups
        // If role is provided in DTO, it will be overridden in UserService
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        } else {
            user.setRole(com.comunityalert.cas.enums.Role.RESIDENT);  // Default to RESIDENT
        }
        
        if (dto.getLocationId() != null) {
            Location location = locationRepo.findById(dto.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));
            user.setLocation(location);
        }
        
        return user;
    }

    /**
     * Update existing User entity from UserDTO (for updates)
     * Does NOT update password - handle that separately
     */
    public void updateEntityFromDTO(User user, UserDTO dto) {
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getLocationId() != null) {
            Location location = locationRepo.findById(dto.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));
            user.setLocation(location);
        }
    }
}