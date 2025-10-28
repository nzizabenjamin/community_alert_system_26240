package com.comunityalert.cas.controller;

import com.comunityalert.cas.dto.LocationDTO;
//import com.comunityalert.cas.model.Location;
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
    public ResponseEntity<List<LocationDTO>> getAll() { 
        return ResponseEntity.ok(service.getAllAsDTO()); 
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getById(@PathVariable UUID id) { 
        return service.getByIdAsDTO(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build()); 
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<LocationDTO> update(@PathVariable UUID id, @RequestBody LocationDTO payload) { 
        return ResponseEntity.ok(service.update(id, payload)); 
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) { 
        service.delete(id); 
        return ResponseEntity.noContent().build(); 
    }
}