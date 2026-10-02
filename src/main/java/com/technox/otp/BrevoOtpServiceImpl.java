package com.technox.otp;

import com.technox.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Production OTP service using Brevo SMTP for email delivery.
 * Falls back to console logging when mail is disabled (dev mode).
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class BrevoOtpServiceImpl implements OtpService {

    private static final int OTP_EXPIRY_SECONDS = 300; // 5 minutes
    private static final int OTP_LENGTH = 6;

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final EmailService emailService;

    private record OtpEntry(String otp, Instant expiryTime, String type) {}

    @Override
    public String generateOtp(String target) {
        return generateOtpWithType(target, "EMAIL");
    }

    public String generateOtpWithType(String target, String type) {
        // Generate cryptographically secure 6-digit OTP
        String otp = String.format("%0" + OTP_LENGTH + "d", secureRandom.nextInt((int) Math.pow(10, OTP_LENGTH)));
        Instant expiryTime = Instant.now().plusSeconds(OTP_EXPIRY_SECONDS);
        otpStore.put(target.toLowerCase().trim(), new OtpEntry(otp, expiryTime, type));

        log.info("[OTP] Generated OTP for target: {} (type: {})", maskTarget(target), type);

        // Send via email if target looks like an email
        if (target.contains("@")) {
            emailService.sendOtpEmail(target.trim(), otp,
                    "EMAIL".equalsIgnoreCase(type) ? "Email Verification" : "Account Verification");
        } else {
            // Mobile OTP — log only for now (SMS gateway can be plugged in later)
            log.info("[OTP-SMS] Mobile OTP {} for {}", otp, maskTarget(target));
        }

        return otp;
    }

    @Override
    public boolean verifyOtp(String target, String otp) {
        String key = target.toLowerCase().trim();
        OtpEntry entry = otpStore.get(key);

        if (entry == null) {
            log.warn("[OTP] No active OTP found for target: {}", maskTarget(target));
            return false;
        }

        if (Instant.now().isAfter(entry.expiryTime())) {
            otpStore.remove(key);
            log.warn("[OTP] OTP expired for target: {}", maskTarget(target));
            return false;
        }

        boolean valid = entry.otp().equals(otp.trim());
        if (valid) {
            otpStore.remove(key);
            log.info("[OTP] Verified successfully for target: {}", maskTarget(target));
        } else {
            log.warn("[OTP] Invalid code attempt for target: {}", maskTarget(target));
        }
        return valid;
    }

    private String maskTarget(String target) {
        if (target == null || target.length() < 4) return "***";
        if (target.contains("@")) {
            int at = target.indexOf('@');
            return target.substring(0, Math.min(3, at)) + "***" + target.substring(at);
        }
        return target.substring(0, 3) + "****" + target.substring(target.length() - 2);
    }
}
