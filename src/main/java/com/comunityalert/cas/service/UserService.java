package com.comunityalert.cas.service;

import java.util.UUID;
import java.util.List;
import org.springframework.stereotype.Service;
import java.util.Optional;
import com.comunityalert.cas.model.User;

import com.comunityalert.cas.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) { 
        this.repo = repo; 
    }
    
    public User create(User u) { 
        return repo.save(u); 
    }
    
    public List<User> getAll() { 
        return repo.findAll(); 
    }
    
    public Optional<User> getById(UUID id) { 
        return repo.findById(id); 
    }
    
    public User update(UUID id, User payload) { 
        User e = repo.findById(id).orElseThrow();
        e.setFullName(payload.getFullName()); 
        e.setEmail(payload.getEmail()); 
        e.setPhoneNumber(payload.getPhoneNumber()); 
        e.setLocation(payload.getLocation());
        return repo.save(e);
    }

    public void delete(UUID id) {
        repo.deleteById(id); 
    }
    
    public List<User> getUsersByProvinceName(String name) { 
        return repo.findUsersByProvinceName(name);
}
    }