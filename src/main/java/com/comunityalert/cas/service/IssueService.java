package com.comunityalert.cas.service;

import java.time.Instant;
import java.util.*;


import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.comunityalert.cas.model.IssueReport;
import com.comunityalert.cas.enums.Status;
import com.comunityalert.cas.repository.IssueRepository;

@Service
public class IssueService {
    
    private final IssueRepository repo;

    public IssueService(IssueRepository repo) { 
        this.repo = repo; 
    }
    public IssueReport create(IssueReport i) { 
        i.setDateReported(Instant.now()); 
        i.setStatus(Status.REPORTED); 
        return repo.save(i); 
    }
    public List<IssueReport> getAll() { 
        return repo.findAll(); 
    }
    public Page<IssueReport> getAll(Pageable pageable) { 
        return repo.findAll(pageable); 
    }
    public Optional<IssueReport> getById(UUID id) {
        return repo.findById(id); 
    }
    public List<IssueReport> getByUser(UUID userId) { 
        return repo.findByReportedById(userId); 
    }
    public IssueReport updateStatus(UUID id, Status status) { 
        IssueReport e = repo.findById(id).orElseThrow(); 
        e.setStatus(status); if (status == Status.RESOLVED) 
        e.setDateResolved(Instant.now()); 
        return repo.save(e); 
    }
    public IssueReport update(UUID id, IssueReport payload) { 
        IssueReport e = repo.findById(id).orElseThrow();
        e.setTitle(payload.getTitle()); 
        e.setDescription(payload.getDescription()); 
        e.setCategory(payload.getCategory()); 
        e.setLocation(payload.getLocation()); 
        return repo.save(e); 
    }
    public void delete(UUID id) { 
        repo.deleteById(id); 
    }
}
