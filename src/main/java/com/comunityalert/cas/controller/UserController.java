package com.comunityalert.cas.controller;

import com.comunityalert.cas.dto.CreateUserDTO;
import com.comunityalert.cas.dto.UserDTO;
import com.comunityalert.cas.service.UserService;

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
    
    public UserController(UserService service) { 
        this.service = service; 
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