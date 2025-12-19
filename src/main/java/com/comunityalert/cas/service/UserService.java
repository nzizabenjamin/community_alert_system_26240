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
import com.comunityalert.cas.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository repo;
    private final UserMapper mapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, UserMapper mapper) { 
        this.repo = repo;
        this.mapper = mapper;
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
     */
    public UserDTO create(CreateUserDTO dto) {
        // Hash password if not already hashed (check if it starts with BCrypt prefix)
        String password = dto.getPassword();
        if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$")) {
            dto.setPassword(passwordEncoder.encode(password));
        }
        
        User user = mapper.toEntity(dto);
        User saved = repo.save(user);
        return mapper.toDTO(saved);
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