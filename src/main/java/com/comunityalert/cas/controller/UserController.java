package com.comunityalert.cas.controller;

import com.comunityalert.cas.model.User;
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

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User u) { 
        
        return ResponseEntity.ok(service.create(u)); 
    }
    
    @GetMapping
    public ResponseEntity<List<User>> getAll() { 
        
        return ResponseEntity.ok(service.getAll()); 
    
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable UUID id) { 
        
        return service.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); 
    
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable UUID id, @RequestBody User payload) { 
        
        return ResponseEntity.ok(service.update(id, payload)); 
    
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) { 
        
        service.delete(id); return ResponseEntity.noContent().build(); 
    
    }


    @GetMapping("/byProvince/{name}")
    public ResponseEntity<List<User>> byProvince(@PathVariable String name) { 
        
        return ResponseEntity.ok(service.getUsersByProvinceName(name)); 
    
    }
}