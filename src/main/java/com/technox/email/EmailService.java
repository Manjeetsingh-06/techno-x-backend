package com.technox.email;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String token);

    void sendPasswordResetEmail(String toEmail, String resetToken);

    void sendPasswordResetOtp(String toEmail, String name, String otp);

    void sendRegistrationConfirmation(String toEmail, String studentName, String eventTitle, String registrationId, String digitalPassId);

    void sendWaitlistNotification(String toEmail, String studentName, String eventTitle, int position);

    void sendWaitlistApprovalNotification(String toEmail, String studentName, String eventTitle, String registrationId);

    void sendEventCancellationNotification(String toEmail, String studentName, String eventTitle, String reason);

    void sendOtpEmail(String toEmail, String otp, String purpose);

    void sendNotificationEmail(String toEmail, String toName, String subject, String title, String body);
}
