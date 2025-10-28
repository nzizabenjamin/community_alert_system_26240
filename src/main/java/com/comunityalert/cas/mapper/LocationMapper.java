package com.comunityalert.cas.mapper;

//import java.util.UUID;

import org.springframework.stereotype.Component;

import com.comunityalert.cas.dto.LocationDTO;
import com.comunityalert.cas.enums.LocationType;
import com.comunityalert.cas.repository.LocationRepository;
import com.comunityalert.cas.model.Location;

@Component
public class LocationMapper {

    public Location toEntity(LocationDTO dto, LocationRepository repo) {
        if (dto == null) return null;

        Location loc = new Location();
        
        loc.setId(dto.getId());
        loc.setName(dto.getName());
        loc.setType(dto.getType() != null ? LocationType.valueOf(dto.getType().toUpperCase()) : null);
        
        if (dto.getParentID() != null) {
            repo.findById(dto.getParentID()).ifPresent(loc::setParent);
        }

        return loc;
    }

    public LocationDTO toDTO(Location entity) {
        if (entity == null) return null;

        LocationDTO dto = new LocationDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType().name());
        dto.setParentID(entity.getParent() != null ? entity.getParent().getId() : null);
        dto.setParentName(entity.getParent() != null ? entity.getParent().getName() : null);
        return dto;
    }

    }
