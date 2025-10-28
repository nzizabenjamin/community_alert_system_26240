package com.comunityalert.cas.dto;

import java.util.UUID;

public class LocationDTO {
    
    private UUID id;
    private String name;
    private String type;
    private UUID parentID;
    private String parentName;
    
    
    public LocationDTO(UUID id, String name, String type, UUID parentID, String parentName) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.parentID = parentID;
        this.parentName = parentName;
    }


    public LocationDTO() {
        //TODO Auto-generated constructor stub
    }


    public UUID getId() {
        return id;
    }


    public void setId(UUID id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }


    public UUID getParentID() {
        return parentID;
    }


    public void setParentID(UUID parentID) {
        this.parentID = parentID;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    
}
