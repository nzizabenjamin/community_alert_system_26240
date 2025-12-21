package com.comunityalert.cas.service;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;

import com.comunityalert.cas.dto.CreateUserDTO;
import com.comunityalert.cas.dto.UserDTO;
import com.comunityalert.cas.mapper.UserMapper;
import com.comunityalert.cas.model.User;
import com.comunityalert.cas.model.Location;
import com.comunityalert.cas.enums.Role;
import com.comunityalert.cas.repository.UserRepository;
import com.comunityalert.cas.repository.LocationRepository;

@Service
public class UserService {
    private final UserRepository repo;
    private final UserMapper mapper;
    private final LocationService locationService;
    private final LocationRepository locationRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, UserMapper mapper, LocationService locationService, LocationRepository locationRepo) { 
        this.repo = repo;
        this.mapper = mapper;
        this.locationService = locationService;
        this.locationRepo = locationRepo;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Expose some raw-entity helper methods used by controllers
    public com.comunityalert.cas.model.User findByEmail(String email) {
        return repo.findByEmail(email).orElse(null);
    }

    public void save(com.comunityalert.cas.model.User user) {
        repo.save(user);
    }

    public com.comunityalert.cas.model.User findByResetToken(String token) {
        return repo.findByResetToken(token).orElse(null);
    }

    public long count() {
        return repo.count();
    }
    
    /**
     * Create new user from CreateUserDTO
     * IMPORTANT: All users created through signup are automatically RESIDENT
     * ADMIN users must be created through admin endpoints, not signup
     */
    public UserDTO create(CreateUserDTO dto) {
        // Hash password if not already hashed (check if it starts with BCrypt prefix)
        String password = dto.getPassword();
        if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$")) {
            dto.setPassword(passwordEncoder.encode(password));
        }
        
        // ✅ CRITICAL: Force RESIDENT role for all signups (ignore any role in DTO)
        dto.setRole(Role.RESIDENT);
        
        // Handle location - support both locationId and villageCode
        if (dto.getLocationId() == null && dto.getVillageCode() != null) {
            // Convert villageCode to locationId
            Location location = findOrCreateLocationFromVillageCode(dto.getVillageCode());
            dto.setLocationId(location.getId());
        }
        
        User user = mapper.toEntity(dto);
        // ✅ Ensure role is set to RESIDENT (double-check)
        user.setRole(Role.RESIDENT);
        User saved = repo.save(user);
        return mapper.toDTO(saved);
    }
    
    /**
     * Find or create a Location entity from a village code
     * Uses RwandaLocations to get the village data, then finds or creates the Location
     */
    private Location findOrCreateLocationFromVillageCode(Integer villageCode) {
        try {
            // Get location data from RwandaLocations
            java.util.Map<String, Object> locationData = locationService.getLocationByVillageCode(villageCode);
            if (locationData == null) {
                throw new RuntimeException("Village code not found: " + villageCode);
            }
            
            // Extract village name from the location data
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> village = (java.util.Map<String, Object>) locationData.get("village");
            String villageName = (String) village.get("name");
            
            // Try to find existing location by name (case-insensitive)
            Optional<Location> existingLocation = locationRepo.findAll().stream()
                .filter(loc -> loc.getName() != null && loc.getName().equalsIgnoreCase(villageName))
                .findFirst();
            
            if (existingLocation.isPresent()) {
                return existingLocation.get();
            }
            
            // Create new location if not found
            Location newLocation = new Location();
            newLocation.setName(villageName);
            newLocation.setType(com.comunityalert.cas.enums.LocationType.VILLAGE);
            // Note: We could set parent location here if needed, but for now just create the village
            
            return locationRepo.save(newLocation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find or create location from village code " + villageCode + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all users as DTOs (passwords hidden)
     */
    public List<UserDTO> getAll() { 
        return repo.findAll().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all users as DTOs with pagination
     */
    public Page<UserDTO> getAll(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toDTO);
    }
    
    /**
     * Get user by ID as DTO
     */
    public Optional<UserDTO> getById(UUID id) { 
        return repo.findById(id)
            .map(mapper::toDTO);
    }
    
    /**
     * Get raw User entity (for internal use only)
     */
    public Optional<User> getUserEntity(UUID id) {
        return repo.findById(id);
    }
    
    /**
     * Update user from UserDTO
     */
    public UserDTO update(UUID id, UserDTO dto) { 
        User existing = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        mapper.updateEntityFromDTO(existing, dto);
        User updated = repo.save(existing);
        return mapper.toDTO(updated);
    }

    /**
     * Delete user
     */
    public void delete(UUID id) {
        repo.deleteById(id); 
    }
    
    /**
     * Get users by province name
     */
    public List<UserDTO> getUsersByProvinceName(String name) { 
        return repo.findUsersByProvinceName(name).stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
}