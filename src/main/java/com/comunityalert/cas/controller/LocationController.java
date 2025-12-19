package com.comunityalert.cas.controller;

import com.comunityalert.cas.dto.LocationDTO;
import com.comunityalert.cas.service.LocationService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "http://localhost:5173")
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
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        // If no pagination params provided, return all locations as a list (for dropdowns)
        if (page == null && size == null) {
            List<LocationDTO> allLocations = service.getAllAsDTO();
            return ResponseEntity.ok(allLocations);
        }
        // Otherwise, return paginated results
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        Sort.Direction dir = Sort.Direction.fromString(sortDir);
        Page<LocationDTO> pageData = service.getAllAsDTO(PageRequest.of(pageNum, pageSize, Sort.by(dir, sortBy)));
        return ResponseEntity.ok(pageData);
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

    /**
     * Search locations globally
     */
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q) {
        List<LocationDTO> allLocations = service.getAllAsDTO();
        List<LocationDTO> results = allLocations.stream()
            .filter(loc -> 
                (loc.getName() != null && loc.getName().toLowerCase().contains(q.toLowerCase())) ||
                (loc.getType() != null && loc.getType().toLowerCase().contains(q.toLowerCase()))
            )
            .toList();
        return ResponseEntity.ok(results);
    }
}