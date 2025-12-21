package com.comunityalert.cas.controller;

import com.comunityalert.cas.dto.LoginRequest;
import com.comunityalert.cas.dto.OTPRequest;
import com.comunityalert.cas.dto.CreateUserDTO;
import com.comunityalert.cas.dto.UserDTO;
import com.comunityalert.cas.model.User;
import com.comunityalert.cas.repository.UserRepository;
import com.comunityalert.cas.service.EmailService;
import com.comunityalert.cas.service.JwtService;
import com.comunityalert.cas.service.OtpService;
import com.comunityalert.cas.service.UserService;
import com.comunityalert.cas.mapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class OTPController {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final UserService userService;
    private final UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public OTPController(UserRepository userRepository, OtpService otpService, JwtService jwtService, 
                        EmailService emailService, UserService userService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        var opt = userRepository.findByEmail(request.getEmail());
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        User user = opt.get();
        String storedPassword = user.getPassword();
        
        // Handle both hashed and plain text passwords (for backward compatibility)
        boolean passwordMatches = false;
        if (storedPassword != null) {
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")) {
                // Password is hashed, use BCrypt to verify
                passwordMatches = passwordEncoder.matches(request.getPassword(), storedPassword);
            } else {
                // Password is plain text (legacy), compare directly
                passwordMatches = storedPassword.equals(request.getPassword());
                // Auto-upgrade: hash the password for next time
                if (passwordMatches) {
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    userRepository.save(user);
                }
            }
        }
        
        if (!passwordMatches) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        String tempToken = jwtService.generateTempToken(user.getId().toString());

        otpService.saveOTP(user.getId(), otp, 5);
        emailService.sendOTP(user.getEmail(), otp);

        return ResponseEntity.ok(Map.of(
                "requiresOTP", true,
                "tempToken", tempToken,
                "message", "OTP sent to your email"
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody OTPRequest request) {
        String userIdStr = jwtService.getUserIdFromTempToken(request.getTempToken());
        if (userIdStr == null) {
            return ResponseEntity.status(401).body("Invalid temp token");
        }

        UUID userId = UUID.fromString(userIdStr);
        String otpCode = request.getOtpCode(); // Use otpCode field
        if (!otpService.verifyOTP(userId, otpCode)) {
            return ResponseEntity.status(401).body("Invalid OTP");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(404).body("User not found");

        // ✅ CRITICAL: Ensure role is set (handle legacy users with null role)
        if (user.getRole() == null) {
            user.setRole(com.comunityalert.cas.enums.Role.RESIDENT);
            user = userRepository.save(user);
        }

        String token = jwtService.generateToken(user);
        UserDTO userDTO = userMapper.toDTO(user);
        
        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", userDTO
        ));
    }

    /**
     * User signup endpoint
     * IMPORTANT: All users created through signup are automatically assigned RESIDENT role
     * ADMIN users cannot be created through signup - they must be created through admin endpoints
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody CreateUserDTO dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        // ✅ CRITICAL: Force RESIDENT role (ignore any role in request body)
        // This ensures no ADMIN can be created through signup
        dto.setRole(com.comunityalert.cas.enums.Role.RESIDENT);

        // Hash password before saving
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // Create user (UserService.create() will also enforce RESIDENT role)
        UserDTO userDTO = userService.create(dto);
        
        // Generate token
        User user = userRepository.findById(userDTO.getId())
            .orElseThrow(() -> new RuntimeException("User not found after creation"));
        String token = jwtService.generateToken(user);
        
        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", userDTO
        ));
    }
}
