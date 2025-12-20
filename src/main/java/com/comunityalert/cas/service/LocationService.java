package com.comunityalert.cas.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.annotation.PostConstruct;

import com.comunityalert.cas.model.Location;
import com.comunityalert.cas.repository.LocationRepository;
import com.comunityalert.cas.dto.LocationDTO;
import com.comunityalert.cas.mapper.LocationMapper;
import com.comunityalert.cas.utils.RwandaLocations;

@Service
public class LocationService {
    
    private final LocationRepository repo;
    private final LocationMapper mapper;
    private RwandaLocations rwandaLocations;
    private final Map<String, List<Map<String, Object>>> cache = new ConcurrentHashMap<>();

    public LocationService(LocationRepository repo, LocationMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    /**
     * Initialize RwandaLocations on startup
     */
    @PostConstruct
    public void init() {
        try {
            // Load from classpath (Spring Boot resources folder)
            java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("locations.json");
            if (is == null) {
                // Try loading from file system as fallback
                String[] possiblePaths = {
                    "locations.json",
                    "src/main/resources/locations.json"
                };
                
                IOException lastException = null;
                for (String path : possiblePaths) {
                    try {
                        rwandaLocations = new RwandaLocations(path);
                        rwandaLocations.load();
                        System.out.println("✅ RwandaLocations initialized successfully from: " + path);
                        return;
                    } catch (IOException e) {
                        lastException = e;
                    }
                }
                throw lastException != null ? lastException : new IOException("Could not find locations.json");
            }
            
            // Copy to temp file since RwandaLocations expects a file path
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("locations", ".json");
            java.nio.file.Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            is.close();
            
            // Initialize RwandaLocations with temp file path
            rwandaLocations = new RwandaLocations(tempFile.toString());
            rwandaLocations.load();
            System.out.println("✅ RwandaLocations initialized successfully from classpath");
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not initialize RwandaLocations: " + e.getMessage());
            System.err.println("   Location hierarchy endpoints will not be available.");
            System.err.println("   Make sure locations.json is in src/main/resources/");
            e.printStackTrace();
        }
    }

    public LocationDTO create(LocationDTO dto) {
        Location entity = mapper.toEntity(dto, repo);
        Location saved = repo.save(entity);
        return mapper.toDTO(saved);
    }

    public List<Location> getAll() {
        return repo.findAll();
    }

    public List<LocationDTO> getAllAsDTO() {
        return repo.findAll().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }

    public Page<LocationDTO> getAllAsDTO(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toDTO);
    }

    public Optional<Location> getById(UUID id) {
        return repo.findById(id);
    }

    public Optional<LocationDTO> getByIdAsDTO(UUID id) {
        return repo.findById(id).map(mapper::toDTO);
    }

    public LocationDTO update(UUID id, LocationDTO dto) {
        Location existing = repo.findById(id).orElseThrow();
        existing.setName(dto.getName());
        
        if (dto.getType() != null) {
            existing.setType(com.comunityalert.cas.enums.LocationType.valueOf(dto.getType().toUpperCase()));
        }
        
        if (dto.getParentID() != null) {
            Location parent = repo.findById(dto.getParentID()).orElseThrow();
            existing.setParent(parent);
        } else {
            existing.setParent(null);
        }
        
        Location saved = repo.save(existing);
        return mapper.toDTO(saved);
    }

    public void delete(UUID id) { 
        repo.deleteById(id); 
    }

    public long count() {
        return repo.count();
    }

    // ========== Rwanda Locations Hierarchy Methods ==========

    /**
     * Get all provinces
     */
    public List<Map<String, Object>> getProvinces() {
        if (rwandaLocations == null) {
            throw new RuntimeException("RwandaLocations not initialized. Check if locations.json exists in src/main/resources/");
        }
        
        String cacheKey = "provinces";
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        
        try {
            List<Map<String, Object>> provinces = rwandaLocations.getProvinces();
            cache.put(cacheKey, provinces);
            return provinces;
        } catch (IOException e) {
            throw new RuntimeException("Error loading provinces: " + e.getMessage(), e);
        }
    }

    /**
     * Get districts by province code
     */
    public List<Map<String, Object>> getDistricts(Integer provinceCode) {
        if (rwandaLocations == null) {
            throw new RuntimeException("RwandaLocations not initialized");
        }
        
        String cacheKey = "districts_" + (provinceCode != null ? provinceCode : "all");
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        
        try {
            List<Map<String, Object>> districts = rwandaLocations.getDistricts(provinceCode);
            cache.put(cacheKey, districts);
            return districts;
        } catch (IOException e) {
            throw new RuntimeException("Error loading districts: " + e.getMessage(), e);
        }
    }

    /**
     * Get sectors by district code
     */
    public List<Map<String, Object>> getSectors(Integer districtCode) {
        if (rwandaLocations == null) {
            throw new RuntimeException("RwandaLocations not initialized");
        }
        
        String cacheKey = "sectors_" + (districtCode != null ? districtCode : "all");
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        
        try {
            List<Map<String, Object>> sectors = rwandaLocations.getSectors(districtCode);
            cache.put(cacheKey, sectors);
            return sectors;
        } catch (IOException e) {
            throw new RuntimeException("Error loading sectors: " + e.getMessage(), e);
        }
    }

    /**
     * Get cells by sector code
     */
    public List<Map<String, Object>> getCells(String sectorCode) {
        if (rwandaLocations == null) {
            throw new RuntimeException("RwandaLocations not initialized");
        }
        
        String cacheKey = "cells_" + (sectorCode != null ? sectorCode : "all");
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        
        try {
            List<Map<String, Object>> cells = rwandaLocations.getCells(sectorCode);
            cache.put(cacheKey, cells);
            return cells;
        } catch (IOException e) {
            throw new RuntimeException("Error loading cells: " + e.getMessage(), e);
        }
    }

    /**
     * Get villages by cell code
     */
    public List<Map<String, Object>> getVillages(Integer cellCode) {
        if (rwandaLocations == null) {
            throw new RuntimeException("RwandaLocations not initialized");
        }
        
        String cacheKey = "villages_" + (cellCode != null ? cellCode : "all");
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        
        try {
            List<Map<String, Object>> villages = rwandaLocations.getVillages(cellCode);
            cache.put(cacheKey, villages);
            return villages;
        } catch (IOException e) {
            throw new RuntimeException("Error loading villages: " + e.getMessage(), e);
        }
    }

    /**
     * Get location hierarchy by village code
     */
    public Map<String, Object> getLocationByVillageCode(Integer villageCode) {
        if (rwandaLocations == null) {
            throw new RuntimeException("RwandaLocations not initialized");
        }
        
        try {
            return rwandaLocations.getLocationByVillageCode(villageCode);
        } catch (IOException e) {
            throw new RuntimeException("Error loading location by village code: " + e.getMessage(), e);
        }
    }

    /**
     * Search locations by name
     */
    public List<Map<String, Object>> searchLocations(String searchTerm, String level) {
        if (rwandaLocations == null) {
            throw new RuntimeException("RwandaLocations not initialized");
        }
        
        try {
            return rwandaLocations.search(searchTerm, level != null ? level : "all");
        } catch (IOException e) {
            throw new RuntimeException("Error searching locations: " + e.getMessage(), e);
        }
    }

    /**
     * Get statistics about locations
     */
    public Map<String, Integer> getLocationStats() {
        if (rwandaLocations == null) {
            throw new RuntimeException("RwandaLocations not initialized");
        }
        
        try {
            return rwandaLocations.getStats();
        } catch (IOException e) {
            throw new RuntimeException("Error getting location stats: " + e.getMessage(), e);
        }
    }
}