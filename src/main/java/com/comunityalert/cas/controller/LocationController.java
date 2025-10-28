package com.comunityalert.cas.controller;

import com.comunityalert.cas.dto.LocationDTO;
import com.comunityalert.cas.model.Location;
import com.comunityalert.cas.service.LocationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationService service;

    public LocationController(LocationService service) { 
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<LocationDTO> create(@RequestBody LocationDTO dto) {
        LocationDTO saved = service.create(dto);
        return ResponseEntity.ok(saved); 
    }
    
    @GetMapping
    public ResponseEntity<List<Location>> getAll() { 
        return ResponseEntity.ok(service.getAll()); 
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Location> getById(@PathVariable UUID id) { 
        return service.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); 
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Location> update(@PathVariable UUID id, @RequestBody Location payload) { 
        return ResponseEntity.ok(service.update(id, payload)); 
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) { 
        service.delete(id); 
        return ResponseEntity.noContent().build(); 
    }
}