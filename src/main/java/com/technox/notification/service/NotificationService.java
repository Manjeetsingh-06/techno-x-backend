package com.technox.notification.service;

import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import com.technox.email.EmailService;
import com.technox.event.entity.Event;
import com.technox.event.repository.EventRepository;
import com.technox.notification.dto.NotificationDto;
import com.technox.notification.dto.SendAnnouncementRequest;
import com.technox.notification.entity.Notification;
import com.technox.notification.entity.NotificationType;
import com.technox.notification.repository.NotificationRepository;
import com.technox.registration.entity.Registration;
import com.technox.registration.repository.RegistrationRepository;
import com.technox.user.entity.User;
import com.technox.user.repository.UserRepository;
import com.technox.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Notification not found: " + notificationId));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Not authorized to modify this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> list = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
    }

    @Transactional
    public int sendAnnouncement(SendAnnouncementRequest request, String senderName) {
        List<User> recipients;

        if (request.getEventId() != null) {
            // Send to students registered for a specific event
            List<User> eventStudents = registrationRepository
                    .findWithFilters(request.getEventId(), null, null, Pageable.unpaged())
                    .getContent()
                    .stream()
                    .map(r -> r.getStudent().getUser())
                    .distinct()
                    .toList();
            recipients = eventStudents.isEmpty() ? userRepository.findAll() : eventStudents;
        } else {
            // Broadcast to all users
            recipients = userRepository.findAll();
        }

        List<Notification> notifications = recipients.stream().map(u -> Notification.builder()
                .recipient(u)
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType() != null ? request.getType() : NotificationType.INFO)
                .eventId(request.getEventId())
                .read(false)
                .senderName(senderName)
                .build()
        ).toList();

        notificationRepository.saveAll(notifications);

        // Multi-channel dispatch: Email + WhatsApp to each recipient
        for (User user : recipients) {
            emailService.sendNotificationEmail(
                    user.getEmail(),
                    user.getName(),
                    "[TECHNO-X] " + request.getTitle(),
                    request.getTitle(),
                    request.getMessage()
            );

            if (user.getMobile() != null && !user.getMobile().isBlank()) {
                whatsAppService.sendWhatsAppNotification(
                        user.getMobile(),
                        user.getName(),
                        request.getTitle(),
                        "Campus Announcement",
                        "TECHNO Campus",
                        request.getMessage()
                );
            }
        }

        log.info("Announcement '{}' dispatched via Email & WhatsApp to {} users by {}", request.getTitle(), notifications.size(), senderName);
        return notifications.size();
    }

    /**
     * Dispatch 24-Hour (1 day before) event reminder to registered students via In-App, Email, and WhatsApp
     */
    @Transactional
    public int sendEventOneDayReminder(Long eventId, String senderName) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found: " + eventId));

        List<Registration> registrations = registrationRepository
                .findWithFilters(eventId, null, null, Pageable.unpaged())
                .getContent();

        if (registrations.isEmpty()) {
            log.info("No registered students found for event '{}' reminder", event.getTitle());
            return 0;
        }

        String title = "⏰ 24H Reminder: " + event.getTitle();
        String dateStr = event.getEventDate() + (event.getStartTime() != null ? " at " + event.getStartTime() : "");
        String venueStr = event.getVenue();
        String messageBody = String.format("Friendly reminder that '%s' is happening tomorrow (%s) at %s. Please have your digital pass QR code ready for gate scanning.",
                event.getTitle(), dateStr, venueStr);

        for (Registration reg : registrations) {
            User studentUser = reg.getStudent().getUser();

            // 1. In-app notification
            notificationRepository.save(Notification.builder()
                    .recipient(studentUser)
                    .title(title)
                    .message(messageBody)
                    .type(NotificationType.EVENT)
                    .eventId(event.getId())
                    .read(false)
                    .senderName(senderName)
                    .build());

            // 2. Email notification via Brevo
            emailService.sendNotificationEmail(
                    studentUser.getEmail(),
                    studentUser.getName(),
                    "[TECHNO-X 24H Reminder] " + event.getTitle(),
                    title,
                    messageBody
            );

            // 3. WhatsApp notification
            if (studentUser.getMobile() != null && !studentUser.getMobile().isBlank()) {
                whatsAppService.sendWhatsAppNotification(
                        studentUser.getMobile(),
                        studentUser.getName(),
                        event.getTitle(),
                        dateStr,
                        venueStr,
                        "Your registered event is tomorrow! Please carry your Digital QR Pass."
                );
            }
        }

        log.info("Dispatched 24H reminder for event '{}' to {} registered students via Email & WhatsApp", event.getTitle(), registrations.size());
        return registrations.size();
    }

    /**
     * Broadcast live event schedule to all registered students via In-App, Email, and WhatsApp
     */
    @Transactional
    public int broadcastLiveSchedule(Long eventId, String senderName) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found: " + eventId));

        List<Registration> registrations = registrationRepository
                .findWithFilters(eventId, null, null, Pageable.unpaged())
                .getContent();

        List<User> recipients = registrations.stream().map(r -> r.getStudent().getUser()).distinct().toList();
        if (recipients.isEmpty()) {
            recipients = userRepository.findAll().stream()
                    .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().name().equals("STUDENT")))
                    .toList();
        }

        String title = "📅 Live Schedule: " + event.getTitle();
        String dateStr = event.getEventDate() + (event.getStartTime() != null ? " (" + event.getStartTime() + " - " + event.getEndTime() + ")" : "");
        String venueStr = event.getVenue();
        String body = String.format("The live schedule for '%s' is now active!\nDate: %s\nVenue: %s\nOrganizer: %s\nEnsure you report 15 minutes before reporting time.",
                event.getTitle(), dateStr, venueStr, event.getOrganizer());

        for (User studentUser : recipients) {
            notificationRepository.save(Notification.builder()
                    .recipient(studentUser)
                    .title(title)
                    .message(body)
                    .type(NotificationType.EVENT)
                    .eventId(event.getId())
                    .read(false)
                    .senderName(senderName)
                    .build());

            emailService.sendNotificationEmail(
                    studentUser.getEmail(),
                    studentUser.getName(),
                    "[TECHNO-X Schedule] " + event.getTitle(),
                    title,
                    body
            );

            if (studentUser.getMobile() != null && !studentUser.getMobile().isBlank()) {
                whatsAppService.sendWhatsAppNotification(
                        studentUser.getMobile(),
                        studentUser.getName(),
                        event.getTitle(),
                        dateStr,
                        venueStr,
                        "Official live schedule has been announced. Check portal for round timings."
                );
            }
        }

        log.info("Broadcasted live schedule for '{}' to {} students via Email & WhatsApp", event.getTitle(), recipients.size());
        return recipients.size();
    }

    public NotificationDto mapToDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .recipientUserId(n.getRecipient().getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .eventId(n.getEventId())
                .read(n.isRead())
                .senderName(n.getSenderName())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
