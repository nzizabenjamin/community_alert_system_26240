package com.comunityalert.cas.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private static class OTPEntry {
        final String otp;
        final Instant expiresAt;

        OTPEntry(String otp, Instant expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<UUID, OTPEntry> store = new ConcurrentHashMap<>();

    public void saveOTP(UUID userId, String otp, int minutes) {
        store.put(userId, new OTPEntry(otp, Instant.now().plus(minutes, ChronoUnit.MINUTES)));
    }

    public boolean verifyOTP(UUID userId, String otp) {
        OTPEntry e = store.get(userId);
        if (e == null) return false;
        if (Instant.now().isAfter(e.expiresAt)) {
            store.remove(userId);
            return false;
        }
        boolean ok = e.otp.equals(otp);
        if (ok) store.remove(userId);
        return ok;
    }
}
