package com.comunityalert.cas.controller;

import com.comunityalert.cas.dto.CreateUserDTO;
import com.comunityalert.cas.dto.UserDTO;
import com.comunityalert.cas.model.User;
import com.comunityalert.cas.service.UserService;
import com.comunityalert.cas.service.JwtService;
import com.comunityalert.cas.mapper.UserMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    
    private final UserService service;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    
    public UserController(UserService service, JwtService jwtService, UserMapper userMapper) { 
        this.service = service;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }
    
    /**
     * Get current authenticated user
     * GET /api/users/me
     * Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            // ✅ CRITICAL: Ensure role is set (handle legacy users with null role)
            if (currentUser.getRole() == null) {
                currentUser.setRole(com.comunityalert.cas.enums.Role.RESIDENT);
                service.save(currentUser);
            }
            
            // Ensure locationId is set in DTO (required by frontend)
            UserDTO userDTO = userMapper.toDTO(currentUser);
            
            // If locationId is still null but location exists, ensure it's set
            if (userDTO.getLocationId() == null && currentUser.getLocation() != null) {
                userDTO.setLocationId(currentUser.getLocation().getId());
                userDTO.setLocationName(currentUser.getLocation().getName());
            }
            
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Helper method to extract current user from Authorization header
     */
    private User getCurrentUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        try {
            String token = authHeader.substring(7);
            String userIdStr = jwtService.getUserIdFromToken(token);
            if (userIdStr == null) {
                return null;
            }
            
            UUID userId = UUID.fromString(userIdStr);
            return service.getUserEntity(userId).orElse(null);
        } catch (Exception e) {
            System.err.println("Error extracting user from token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Create new user
     * Request body: CreateUserDTO (includes password)
     * Response: UserDTO (password hidden)
     */
    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody CreateUserDTO dto) { 
        return ResponseEntity.ok(service.create(dto)); 
    }
    
    /**
     * Get all users (passwords hidden) with pagination
     */
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        Sort.Direction dir = Sort.Direction.fromString(sortDir);
        Page<UserDTO> pageData = service.getAll(PageRequest.of(page, size, Sort.by(dir, sortBy)));
        return ResponseEntity.ok(pageData);
    }

    /**
     * Get user by ID (password hidden)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable UUID id) { 
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build()); 
    }
    
    /**
     * Update user
     * Request body: UserDTO (no password field)
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable UUID id, @RequestBody UserDTO payload) { 
        return ResponseEntity.ok(service.update(id, payload)); 
    }
    
    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) { 
        service.delete(id); 
        return ResponseEntity.noContent().build(); 
    }

    /**
     * Get users by province name
     */
    @GetMapping("/byProvince/{name}")
    public ResponseEntity<List<UserDTO>> byProvince(@PathVariable String name) { 
        return ResponseEntity.ok(service.getUsersByProvinceName(name)); 
    }

    /**
     * Search users globally
     */
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q) {
        // Search by name, email, or phone
        List<UserDTO> allUsers = service.getAll();
        List<UserDTO> results = allUsers.stream()
            .filter(user -> 
                (user.getFullName() != null && user.getFullName().toLowerCase().contains(q.toLowerCase())) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(q.toLowerCase())) ||
                (user.getPhoneNumber() != null && user.getPhoneNumber().contains(q))
            )
            .toList();
        return ResponseEntity.ok(results);
    }
}