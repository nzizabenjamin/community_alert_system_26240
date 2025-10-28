package com.comunityalert.cas.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.comunityalert.cas.model.Location;
import com.comunityalert.cas.repository.LocationRepository;
import com.comunityalert.cas.dto.LocationDTO;
import com.comunityalert.cas.mapper.LocationMapper;

@Service
public class LocationService {
    
    private final LocationRepository repo;
    private LocationMapper mapper;

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

   /* public Location create(Location l) {
        return repo.save(l);
    }

    public List<Location> getAll() {
        return repo.findAll();
    } */

    public Optional<Location> getById(UUID id) {
        return repo.findById(id);
    }

    public Location update(UUID id, Location payload) {
        Location existing = repo.findById(id).orElseThrow();
        existing.setName(payload.getName());
        existing.setType(payload.getType());
        existing.setParent(payload.getParent());
        return repo.save(existing);
    }
    public void delete(UUID id) { repo.deleteById(id); }
}

