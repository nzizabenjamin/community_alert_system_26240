package com.comunityalert.cas.controller;

import com.comunityalert.cas.model.IssueReport;
import com.comunityalert.cas.enums.Status;

import com.comunityalert.cas.service.IssueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/issues")
public class IssueController {
    private final IssueService service;
    
    public IssueController(IssueService service) { 
        this.service = service; 
    }


@PostMapping
public ResponseEntity<IssueReport> create(@RequestBody IssueReport i) { 
    
    return ResponseEntity.ok(service.create(i)); 

}

@GetMapping
public ResponseEntity<List<IssueReport>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    
    Page<IssueReport> p = service.getAll(PageRequest.of(page, size));
    
    return ResponseEntity.ok(p.getContent());
}

@GetMapping("/{id}")
public ResponseEntity<IssueReport> getById(@PathVariable UUID id) { 
    
    return service.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); 

}

@PutMapping("/{id}")
public ResponseEntity<IssueReport> update(@PathVariable UUID id, @RequestBody IssueReport payload) { 
    
    return ResponseEntity.ok(service.update(id, payload)); 

}

@PutMapping("/{id}/status")
public ResponseEntity<IssueReport> updateStatus(@PathVariable UUID id, @RequestParam Status status) { 
    
    return ResponseEntity.ok(service.updateStatus(id, status)); 

}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable UUID id) { 
    
    service.delete(id); return ResponseEntity.noContent().build(); 

}

}