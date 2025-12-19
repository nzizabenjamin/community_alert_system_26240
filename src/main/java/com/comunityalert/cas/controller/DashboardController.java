package com.comunityalert.cas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.comunityalert.cas.model.IssueReport;
import com.comunityalert.cas.model.User;
import com.comunityalert.cas.service.IssueService;
import com.comunityalert.cas.service.UserService;
import com.comunityalert.cas.service.LocationService;
import com.comunityalert.cas.service.JwtService;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {
    
    @Autowired
    private IssueService issueService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LocationService locationService;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * Helper method to get current user from Authorization header
     */
    private User getCurrentUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authHeader.substring(7);
        String userIdStr = jwtService.getUserIdFromToken(token);
        
        if (userIdStr == null) {
            return null;
        }
        
        try {
            UUID userId = UUID.fromString(userIdStr);
            return userService.getUserEntity(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Map<String, Object> stats = new HashMap<>();
            User currentUser = getCurrentUser(authHeader);
            
            if (currentUser == null) {
                // Return empty stats if not authenticated
                stats.put("totalIssues", 0);
                stats.put("reportedIssues", 0);
                stats.put("inProgressIssues", 0);
                stats.put("resolvedIssues", 0);
                stats.put("totalUsers", 0);
                stats.put("totalLocations", 0);
                stats.put("recentIssues", List.of());
                stats.put("issuesByCategory", List.of());
                stats.put("issuesByLocation", List.of());
                return ResponseEntity.ok(stats);
            }
            
            // Count issues by status with role-based filtering
            long totalIssues = issueService.count(currentUser);
            long reportedIssues = issueService.countByStatus("REPORTED", currentUser);
            long inProgressIssues = issueService.countByStatus("IN_PROGRESS", currentUser);
            long resolvedIssues = issueService.countByStatus("RESOLVED", currentUser);
            
            stats.put("totalIssues", totalIssues);
            stats.put("reportedIssues", reportedIssues);
            stats.put("inProgressIssues", inProgressIssues);
            stats.put("resolvedIssues", resolvedIssues);
            
            // Other statistics (only show for ADMIN)
            if (currentUser.getRole() == com.comunityalert.cas.enums.Role.ADMIN) {
                stats.put("totalUsers", userService.count());
                stats.put("totalLocations", locationService.count());
            } else {
                stats.put("totalUsers", 0);
                stats.put("totalLocations", 0);
            }
            
            // Recent issues with role-based filtering
            List<IssueReport> recentIssues = issueService.findTop5ByOrderByDateReportedDesc(currentUser);
            stats.put("recentIssues", recentIssues);
            
            // Issues by category with role-based filtering
            List<Map<String, Object>> issuesByCategory = issueService.countByCategory(currentUser);
            stats.put("issuesByCategory", issuesByCategory);
            
            // Issues by location with role-based filtering
            List<Map<String, Object>> issuesByLocation = issueService.countByLocation(currentUser);
            stats.put("issuesByLocation", issuesByLocation);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load dashboard stats: " + e.getMessage()));
        }
    }
}