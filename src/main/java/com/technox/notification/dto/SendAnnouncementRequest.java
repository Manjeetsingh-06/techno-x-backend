package com.technox.notification.dto;

import com.technox.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendAnnouncementRequest {

    private Long eventId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @Builder.Default
    private NotificationType type = NotificationType.INFO;
}
