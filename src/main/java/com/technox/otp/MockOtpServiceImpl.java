package com.technox.otp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MockOtpServiceImpl implements OtpService {

    private static final int OTP_EXPIRY_SECONDS = 300; // 5 minutes
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private record OtpEntry(String otp, Instant expiryTime) {}

    @Override
    public String generateOtp(String mobile) {
        String otp = String.format("%06d", random.nextInt(1000000));
        Instant expiryTime = Instant.now().plusSeconds(OTP_EXPIRY_SECONDS);
        otpStore.put(mobile, new OtpEntry(otp, expiryTime));
        log.info("[MOCK OTP] Generated OTP {} for mobile: {}. Valid for 5 minutes.", otp, mobile);
        return otp;
    }

    @Override
    public boolean verifyOtp(String mobile, String otp) {
        OtpEntry entry = otpStore.get(mobile);
        if (entry == null) {
            log.warn("[MOCK OTP] No OTP found for mobile: {}", mobile);
            return false;
        }

        if (Instant.now().isAfter(entry.expiryTime())) {
            otpStore.remove(mobile);
            log.warn("[MOCK OTP] OTP expired for mobile: {}", mobile);
            return false;
        }

        boolean isValid = entry.otp().equals(otp);
        if (isValid) {
            otpStore.remove(mobile);
            log.info("[MOCK OTP] OTP verified successfully for mobile: {}", mobile);
        } else {
            log.warn("[MOCK OTP] Invalid OTP attempt for mobile: {}", mobile);
        }
        return isValid;
    }
}
