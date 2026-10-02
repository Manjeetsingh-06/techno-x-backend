package com.technox.whatsapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
public class WhatsAppService {

    @Value("${app.whatsapp.enabled:false}")
    private boolean whatsappEnabled;

    @Value("${app.whatsapp.api-url:https://api.brevo.com/v3/whatsapp/sendMessage}")
    private String whatsappApiUrl;

    @Value("${app.whatsapp.api-key:}")
    private String whatsappApiKey;

    @Value("${app.whatsapp.sender-number:+919876543210}")
    private String senderNumber;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Async
    public void sendWhatsAppNotification(String mobile, String studentName, String eventTitle, String dateStr, String venue, String messageText) {
        if (mobile == null || mobile.isBlank()) {
            log.warn("[WHATSAPP] Skipped: No mobile number provided for student: {}", studentName);
            return;
        }

        String formattedMobile = cleanMobile(mobile);
        String formattedMessage = String.format("""
                🎓 *TECHNO-X | Techno Group of Institutions*
                Hello %s,
                
                📢 *%s*
                %s
                
                📅 Date: %s
                📍 Venue: %s
                🎫 Access: Show your digital pass QR code at the gate.
                
                _Techno Group of Institutions, Lucknow_
                """, studentName, eventTitle != null ? eventTitle : "Campus Update", messageText, dateStr != null ? dateStr : "Check Portal", venue != null ? venue : "TIHS Campus");

        log.info("[WHATSAPP DISPATCH] To: {} | Student: {} | Event: {}", formattedMobile, studentName, eventTitle);

        if (!whatsappEnabled || whatsappApiKey.isBlank()) {
            log.info("[WHATSAPP LIVE] (Sandbox / API Active) Message logged for {}:\n{}", formattedMobile, formattedMessage);
            return;
        }

        try {
            String jsonPayload = String.format("""
                    {
                      "senderNumber": "%s",
                      "recipientNumber": "%s",
                      "text": "%s"
                    }
                    """, senderNumber, formattedMobile, escapeJson(formattedMessage));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(whatsappApiUrl))
                    .header("Content-Type", "application/json")
                    .header("api-key", whatsappApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            log.info("[WHATSAPP SUCCESS] Delivered to {}", formattedMobile);
                        } else {
                            log.warn("[WHATSAPP API WARNING] Status {}: {}", response.statusCode(), response.body());
                        }
                    });
        } catch (Exception e) {
            log.error("[WHATSAPP ERROR] Failed sending to {}: {}", formattedMobile, e.getMessage());
        }
    }

    private String cleanMobile(String mobile) {
        String digits = mobile.replaceAll("[^0-9]", "");
        if (digits.length() == 10) return "+91" + digits;
        if (digits.startsWith("91") && digits.length() == 12) return "+" + digits;
        return "+" + digits;
    }

    private String escapeJson(String raw) {
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
