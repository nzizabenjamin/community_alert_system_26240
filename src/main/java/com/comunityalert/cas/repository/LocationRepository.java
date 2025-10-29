package com.comunityalert.cas.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.comunityalert.cas.enums.LocationType;
import com.comunityalert.cas.model.Location;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    List<Location> findByParentId(UUID parentId);

    //Optional<Location>findById(UUID id);

    List<Location> findByType(LocationType type);

    List<Location> findByNameContainingIgnoreCase(String name);
    
    boolean existsByNameAndType(String name, LocationType type);

    
}
