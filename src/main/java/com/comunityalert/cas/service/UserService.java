package com.comunityalert.cas.service;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
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

    public UserService(UserRepository repo, UserMapper mapper) { 
        this.repo = repo;
        this.mapper = mapper;
    }
    
    /**
     * Create new user from CreateUserDTO
     */
    public UserDTO create(CreateUserDTO dto) {
        // TODO: In production, hash the password here!
        // dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        
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