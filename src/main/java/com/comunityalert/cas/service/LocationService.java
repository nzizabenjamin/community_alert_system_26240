package com.comunityalert.cas.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.comunityalert.cas.model.Location;
import com.comunityalert.cas.repository.LocationRepository;
import com.comunityalert.cas.dto.LocationDTO;
import com.comunityalert.cas.mapper.LocationMapper;

@Service
public class LocationService {
    
    private final LocationRepository repo;
    private final LocationMapper mapper;

    public LocationService(LocationRepository repo, LocationMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
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
}