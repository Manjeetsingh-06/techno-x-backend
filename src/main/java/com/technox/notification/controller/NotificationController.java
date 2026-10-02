package com.technox.notification.controller;

import com.technox.common.dto.ApiResponse;
import com.technox.notification.dto.NotificationDto;
import com.technox.notification.dto.SendAnnouncementRequest;
import com.technox.notification.service.NotificationService;
import com.technox.user.entity.User;
import com.technox.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for user alerts, system notices, WhatsApp and broadcast announcements")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get user notifications list")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getMyNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<NotificationDto> list = notificationService.getUserNotifications(user.getId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread notifications for current user")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all user notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    @PostMapping("/announce")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Broadcast announcement to students via in-app, email, and WhatsApp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendAnnouncement(
            @Valid @RequestBody SendAnnouncementRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User sender = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        int count = notificationService.sendAnnouncement(request, sender.getName());
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("recipientsCount", count, "status", "DISPATCHED", "channels", List.of("IN_APP", "EMAIL", "WHATSAPP")),
                "Announcement broadcast successfully via Email and WhatsApp to " + count + " users."
        ));
    }

    @PostMapping("/remind/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    @Operation(summary = "Send 1-day before reminder to all registered students via Email & WhatsApp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendOneDayReminder(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User sender = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        int notified = notificationService.sendEventOneDayReminder(eventId, sender.getName());
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("notifiedCount", notified, "status", "SENT", "channels", List.of("IN_APP", "EMAIL", "WHATSAPP")),
                "1-Day reminder sent to " + notified + " registered students via Email and WhatsApp."
        ));
    }

    @PostMapping("/schedule/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Broadcast live event schedule to registered students via Email & WhatsApp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> broadcastSchedule(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User sender = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        int notified = notificationService.broadcastLiveSchedule(eventId, sender.getName());
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("notifiedCount", notified, "status", "BROADCASTED", "channels", List.of("IN_APP", "EMAIL", "WHATSAPP")),
                "Live schedule broadcast sent to " + notified + " students via Email and WhatsApp."
        ));
    }
}
