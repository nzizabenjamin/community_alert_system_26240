package com.comunityalert.cas.controller;

import com.comunityalert.cas.model.User;
import com.comunityalert.cas.service.UserService;
import com.comunityalert.cas.service.EmailService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        User user = userService.findByEmail(email);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("Email not found");
        }
        
        // Generate reset token (valid for 1 hour)
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(Instant.now().plus(1, ChronoUnit.HOURS));
        userService.save(user);
        
        // Send email
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        
        return ResponseEntity.ok("Reset email sent");
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword"); // Frontend sends "newPassword"
        
        User user = userService.findByResetToken(token);
        
        if (user == null || user.getResetTokenExpiry().isBefore(Instant.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userService.save(user);
        
        return ResponseEntity.ok("Password reset successful");
    }
}