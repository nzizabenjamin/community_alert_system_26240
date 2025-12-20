package com.comunityalert.cas.controller;

import com.comunityalert.cas.dto.LocationDTO;
import com.comunityalert.cas.service.LocationService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
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
     * Search locations globally (database locations)
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

    // ========== Rwanda Locations Hierarchy Endpoints ==========

    /**
     * Get all provinces
     * GET /api/locations/provinces
     */
    @GetMapping("/provinces")
    public ResponseEntity<?> getProvinces() {
        try {
            return ResponseEntity.ok(service.getProvinces());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get districts by province code
     * GET /api/locations/districts?provinceCode=1
     */
    @GetMapping("/districts")
    public ResponseEntity<?> getDistricts(@RequestParam(required = false) Integer provinceCode) {
        try {
            return ResponseEntity.ok(service.getDistricts(provinceCode));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get sectors by district code
     * GET /api/locations/sectors?districtCode=101
     */
    @GetMapping("/sectors")
    public ResponseEntity<?> getSectors(@RequestParam(required = false) Integer districtCode) {
        try {
            return ResponseEntity.ok(service.getSectors(districtCode));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get cells by sector code
     * GET /api/locations/cells?sectorCode=010101
     */
    @GetMapping("/cells")
    public ResponseEntity<?> getCells(@RequestParam(required = false) String sectorCode) {
        try {
            return ResponseEntity.ok(service.getCells(sectorCode));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get villages by cell code
     * GET /api/locations/villages?cellCode=10101
     */
    @GetMapping("/villages")
    public ResponseEntity<?> getVillages(@RequestParam(required = false) Integer cellCode) {
        try {
            return ResponseEntity.ok(service.getVillages(cellCode));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get complete location hierarchy by village code
     * GET /api/locations/village/{villageCode}
     */
    @GetMapping("/village/{villageCode}")
    public ResponseEntity<?> getLocationByVillageCode(@PathVariable Integer villageCode) {
        try {
            java.util.Map<String, Object> location = service.getLocationByVillageCode(villageCode);
            if (location == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(location);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Search Rwanda locations by name
     * GET /api/locations/rwanda/search?q=kigali&level=all
     */
    @GetMapping("/rwanda/search")
    public ResponseEntity<?> searchRwandaLocations(
            @RequestParam String q,
            @RequestParam(required = false, defaultValue = "all") String level) {
        try {
            return ResponseEntity.ok(service.searchLocations(q, level));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get location statistics
     * GET /api/locations/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getLocationStats() {
        try {
            return ResponseEntity.ok(service.getLocationStats());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}