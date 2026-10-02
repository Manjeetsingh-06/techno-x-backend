package com.technox.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockEmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(MockEmailServiceImpl.class);

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        log.info("[MOCK EMAIL] Verification link for {}: https://technox.tgi.ac.in/verify-email?token={}", toEmail, token);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        log.info("[MOCK EMAIL] Password reset link for {}: https://technox.tgi.ac.in/reset-password?token={}", toEmail, resetToken);
    }

    @Override
    public void sendPasswordResetOtp(String toEmail, String name, String otp) {
        log.info("[MOCK EMAIL] Password reset OTP {} sent to {} ({})", otp, name, toEmail);
    }

    @Override
    public void sendRegistrationConfirmation(String toEmail, String studentName, String eventTitle, String registrationId, String digitalPassId) {
        log.info("[MOCK EMAIL] Event confirmed for {} ({}) - Event: {}, RegID: {}, Pass: {}", studentName, toEmail, eventTitle, registrationId, digitalPassId);
    }

    @Override
    public void sendWaitlistNotification(String toEmail, String studentName, String eventTitle, int position) {
        log.info("[MOCK EMAIL] Waitlisted notification for {} ({}) - Event: {}, Queue Position: #{}", studentName, toEmail, eventTitle, position);
    }

    @Override
    public void sendWaitlistApprovalNotification(String toEmail, String studentName, String eventTitle, String registrationId) {
        log.info("[MOCK EMAIL] Waitlist PROMOTED for {} ({}) - Event: {}, Pass: {}", studentName, toEmail, eventTitle, registrationId);
    }

    @Override
    public void sendEventCancellationNotification(String toEmail, String studentName, String eventTitle, String reason) {
        log.info("[MOCK EMAIL] Event cancelled notification for {} ({}) - Event: {}, Reason: {}", studentName, toEmail, eventTitle, reason);
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp, String purpose) {
        log.info("[MOCK EMAIL] OTP {} sent to {} for purpose: {}", otp, toEmail, purpose);
    }

    @Override
    public void sendNotificationEmail(String toEmail, String toName, String subject, String title, String body) {
        log.info("[MOCK EMAIL] Notification to {} ({}) — {}: {}", toName, toEmail, subject, title);
    }
}
