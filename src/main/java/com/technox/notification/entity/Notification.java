package com.technox.notification.entity;

import com.technox.common.entity.BaseEntity;
import com.technox.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipient;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    @Builder.Default
    private NotificationType type = NotificationType.INFO;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "sender_name", length = 100)
    private String senderName;
}
