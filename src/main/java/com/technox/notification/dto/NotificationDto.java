package com.technox.notification.dto;

import com.technox.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private Long recipientUserId;
    private String title;
    private String message;
    private NotificationType type;
    private Long eventId;
    private boolean read;
    private String senderName;
    private LocalDateTime createdAt;
}
