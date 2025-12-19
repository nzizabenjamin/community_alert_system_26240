package com.comunityalert.cas.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.comunityalert.cas.model.User;

@Service
public class JwtService {
    private final Map<String, String> tempTokenToUserId = new ConcurrentHashMap<>();
    // Store token -> userId mapping for authentication
    private final Map<String, String> tokenToUserId = new ConcurrentHashMap<>();

    public String generateTempToken(String userId) {
        String token = UUID.randomUUID().toString();
        tempTokenToUserId.put(token, userId);
        return token;
    }

    public String getUserIdFromTempToken(String tempToken) {
        return tempTokenToUserId.get(tempToken);
    }

    public String generateToken(User user) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, user.getId().toString());
        return token;
    }

    public String getUserIdFromToken(String token) {
        return tokenToUserId.get(token);
    }

    public void invalidateToken(String token) {
        tokenToUserId.remove(token);
    }
}
