package com.comunityalert.cas.controller;

import com.comunityalert.cas.dto.CreateUserDTO;
import com.comunityalert.cas.dto.UserDTO;
import com.comunityalert.cas.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
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
     * Get all users (passwords hidden)
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() { 
        return ResponseEntity.ok(service.getAll()); 
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
}