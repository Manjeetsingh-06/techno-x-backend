package com.technox.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class BrevoEmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from-address:noreply@technox.tgi.ac.in}")
    private String fromAddress;

    @Value("${app.mail.from-name:TECHNO-X | TGI Campus Portal}")
    private String fromName;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        sendNotificationEmail(toEmail, "Student", "Verify Your TECHNO-X Email",
                "Email Verification", "Please verify your email using this link: https://technox.tgi.ac.in/verify-email?token=" + token);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        sendNotificationEmail(toEmail, "User", "Reset Your TECHNO-X Password",
                "Password Reset", "Please reset your password using this link: https://technox.tgi.ac.in/reset-password?token=" + resetToken);
    }

    @Override
    public void sendPasswordResetOtp(String toEmail, String name, String otp) {
        sendOtpEmail(toEmail, otp, "Password Reset");
    }

    @Override
    public void sendRegistrationConfirmation(String toEmail, String studentName, String eventTitle, String registrationId, String digitalPassId) {
        sendNotificationEmail(toEmail, studentName, "Registration Confirmed — " + eventTitle,
                "Event Confirmed!", "Your registration for <strong>" + eventTitle + "</strong> is confirmed.<br/>Registration ID: " + registrationId + "<br/>Pass ID: " + digitalPassId);
    }

    @Override
    public void sendWaitlistNotification(String toEmail, String studentName, String eventTitle, int position) {
        sendNotificationEmail(toEmail, studentName, "Waitlist Update — " + eventTitle,
                "Waitlist Position #" + position, "You are currently at position #" + position + " on the waitlist for " + eventTitle);
    }

    @Override
    public void sendWaitlistApprovalNotification(String toEmail, String studentName, String eventTitle, String registrationId) {
        sendNotificationEmail(toEmail, studentName, "Seat Confirmed! — " + eventTitle,
                "Promoted from Waitlist!", "Great news! Your waitlist seat has been confirmed for <strong>" + eventTitle + "</strong>. Registration ID: " + registrationId);
    }

    @Override
    public void sendEventCancellationNotification(String toEmail, String studentName, String eventTitle, String reason) {
        sendNotificationEmail(toEmail, studentName, "Event Cancelled — " + eventTitle,
                "Cancellation Notice", "The event <strong>" + eventTitle + "</strong> has been cancelled. Reason: " + reason);
    }

    @Value("${app.brevo.api-key:${BREVO_API_KEY:}}")
    private String brevoApiKey;

    @Override
    @Async
    public void sendOtpEmail(String toEmail, String otp, String purpose) {
        String html = buildOtpEmailHtml(otp, purpose);
        String subject = "Your TECHNO-X Verification Code — " + otp;

        // 1. Try Brevo Transactional Email REST API first (port 443 HTTPS - most reliable)
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                sendViaBrevoApi(toEmail, "Student", subject, html);
                log.info("[BREVO-API-OTP] Dispatched OTP {} to {} via Brevo REST API", otp, toEmail);
                return;
            } catch (Exception e) {
                log.error("[BREVO-API-OTP] Failed sending via Brevo API: {}", e.getMessage());
            }
        }

        // 2. Fall back to Brevo SMTP
        if (mailEnabled) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromAddress, fromName);
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(html, true);
                mailSender.send(message);
                log.info("[BREVO-SMTP-OTP] Sent live OTP email via Brevo SMTP to: {}", toEmail);
                return;
            } catch (Exception e) {
                log.error("[BREVO-SMTP-OTP] Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            }
        }

        // 3. Fallback / Dev mode log
        log.info("[BREVO-OTP-DEV] Real OTP for {} -> {} (purpose: {}). [Set BREVO_API_KEY to send live email]", toEmail, otp, purpose);
    }

    @Override
    @Async
    public void sendNotificationEmail(String toEmail, String toName, String subject, String title, String body) {
        String html = buildNotificationEmailHtml(toName, title, body);

        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                sendViaBrevoApi(toEmail, toName, subject, html);
                log.info("[BREVO-API-NOTIFY] Sent notification to {} via Brevo REST API", toEmail);
                return;
            } catch (Exception e) {
                log.error("[BREVO-API-NOTIFY] Failed to send via Brevo API: {}", e.getMessage());
            }
        }

        if (mailEnabled) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromAddress, fromName);
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(html, true);
                mailSender.send(message);
                log.info("[BREVO-SMTP-NOTIFY] Sent notification via Brevo SMTP to: {}", toEmail);
                return;
            } catch (Exception e) {
                log.error("[BREVO-SMTP-NOTIFY] Failed to send notification to {}: {}", toEmail, e.getMessage());
            }
        }

        log.info("[BREVO-NOTIFY-DEV] Notification to {} — {}: {}", toEmail, subject, title);
    }

    private void sendViaBrevoApi(String toEmail, String toName, String subject, String htmlContent) throws Exception {
        // Build payload using Jackson ObjectMapper so HTML content with quotes/newlines is safe
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        com.fasterxml.jackson.databind.node.ObjectNode payload = mapper.createObjectNode();

        com.fasterxml.jackson.databind.node.ObjectNode sender = payload.putObject("sender");
        sender.put("name", fromName != null ? fromName : "TECHNO-X");
        sender.put("email", fromAddress != null ? fromAddress : "noreply@technox.tgi.ac.in");

        com.fasterxml.jackson.databind.node.ArrayNode toArray = payload.putArray("to");
        com.fasterxml.jackson.databind.node.ObjectNode toObj = toArray.addObject();
        toObj.put("email", toEmail);
        toObj.put("name", toName != null ? toName : toEmail);

        payload.put("subject", subject);
        payload.put("htmlContent", htmlContent);

        String jsonPayload = mapper.writeValueAsString(payload);

        java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("api-key", brevoApiKey.trim())
                .timeout(java.time.Duration.ofSeconds(15))
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload, java.nio.charset.StandardCharsets.UTF_8))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            log.info("[BREVO-API] Email delivered to {} (status: {})", toEmail, response.statusCode());
        } else {
            log.error("[BREVO-API] Brevo API error {} for {}: {}", response.statusCode(), toEmail, response.body());
            throw new RuntimeException("Brevo API returned " + response.statusCode() + ": " + response.body());
        }
    }

    private String buildOtpEmailHtml(String otp, String purpose) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>
            <body style='margin:0;padding:0;background:#0f172a;font-family:Arial,sans-serif;'>
              <div style='max-width:520px;margin:40px auto;background:#1e293b;border-radius:16px;overflow:hidden;border:1px solid #334155;'>
                <div style='background:linear-gradient(135deg,#1e3a5f,#0f172a);padding:28px 32px;text-align:center;'>
                  <div style='display:inline-block;background:#f59e0b;border-radius:10px;padding:8px 16px;margin-bottom:12px;'>
                    <span style='color:#1e293b;font-weight:900;font-size:18px;letter-spacing:2px;'>TECHNO-X</span>
                  </div>
                  <h1 style='color:#ffffff;margin:0;font-size:22px;font-weight:800;'>Email Verification</h1>
                  <p style='color:#94a3b8;margin:6px 0 0;font-size:13px;'>Techno Group of Institutions — Campus Portal</p>
                </div>
                <div style='padding:32px;'>
                  <p style='color:#cbd5e1;font-size:14px;margin:0 0 8px;'>Purpose: <strong style='color:#f59e0b;'>%s</strong></p>
                  <p style='color:#94a3b8;font-size:13px;margin:0 0 24px;'>Use the code below to complete your verification. Valid for 5 minutes.</p>
                  <div style='background:#0f172a;border:2px dashed #334155;border-radius:12px;padding:24px;text-align:center;'>
                    <div style='color:#f59e0b;font-size:40px;font-weight:900;letter-spacing:12px;font-family:monospace;'>%s</div>
                    <p style='color:#64748b;font-size:11px;margin:8px 0 0;'>Do not share this code with anyone</p>
                  </div>
                  <p style='color:#475569;font-size:12px;margin:24px 0 0;text-align:center;'>If you didn't request this, please ignore this email.</p>
                </div>
                <div style='background:#0f172a;padding:16px 32px;text-align:center;border-top:1px solid #1e293b;'>
                  <p style='color:#334155;font-size:11px;margin:0;'>© 2026 Techno Group of Institutions, Lucknow. All rights reserved.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(purpose, otp);
    }

    private String buildNotificationEmailHtml(String toName, String title, String body) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset='UTF-8'></head>
            <body style='margin:0;padding:0;background:#0f172a;font-family:Arial,sans-serif;'>
              <div style='max-width:520px;margin:40px auto;background:#1e293b;border-radius:16px;overflow:hidden;border:1px solid #334155;'>
                <div style='background:linear-gradient(135deg,#1e3a5f,#0f172a);padding:28px 32px;'>
                  <div style='display:inline-block;background:#f59e0b;border-radius:8px;padding:6px 14px;margin-bottom:12px;'>
                    <span style='color:#1e293b;font-weight:900;font-size:15px;'>TECHNO-X</span>
                  </div>
                  <h2 style='color:#ffffff;margin:0;font-size:20px;font-weight:800;'>%s</h2>
                </div>
                <div style='padding:28px 32px;'>
                  <p style='color:#e2e8f0;font-size:15px;margin:0 0 16px;'>Dear <strong>%s</strong>,</p>
                  <div style='color:#cbd5e1;font-size:14px;line-height:1.7;background:#0f172a;border-radius:10px;padding:20px;border-left:4px solid #f59e0b;'>
                    %s
                  </div>
                  <p style='color:#475569;font-size:12px;margin:24px 0 0;'>This is an automated message from TECHNO-X Campus Portal.</p>
                </div>
                <div style='background:#0f172a;padding:14px 32px;text-align:center;border-top:1px solid #1e293b;'>
                  <p style='color:#334155;font-size:11px;margin:0;'>© 2026 Techno Group of Institutions, Lucknow</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(title, toName, body);
    }
}
