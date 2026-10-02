package com.technox.event.service;

import com.technox.email.EmailService;
import com.technox.event.entity.Event;
import com.technox.event.entity.EventStatus;
import com.technox.event.repository.EventRepository;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled service to:
 * 1. Send 24-hour reminder to registered students before events
 * 2. Auto-close registrations past deadline
 * 3. Auto-mark events as ONGOING on their start date
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventSchedulerService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;
    private final UserRepository userRepository;

    /**
     * Every day at 8:00 AM — send 24h reminder to students registered for tomorrow's events
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendTomorrowEventReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("[SCHEDULER] Sending 24h event reminders for events on: {}", tomorrow);

        List<Event> tomorrowEvents = eventRepository.findByEventDateAndStatusIn(
                tomorrow,
                List.of(EventStatus.PUBLISHED, EventStatus.APPROVED)
        );

        for (Event event : tomorrowEvents) {
            List<Registration> registrations = registrationRepository
                    .findWithFilters(event.getId(), null, null, Pageable.unpaged()).getContent();

            log.info("[SCHEDULER] Sending reminders for '{}' to {} students", event.getTitle(), registrations.size());

            for (Registration reg : registrations) {
                User student = reg.getStudent().getUser();
                String title = "Event Tomorrow: " + event.getTitle();
                String message = String.format(
                        "Reminder: '%s' is scheduled for tomorrow, %s at %s. Venue: %s. Please carry your digital pass.",
                        event.getTitle(),
                        event.getEventDate(),
                        event.getStartTime() != null ? event.getStartTime().toString() : "TBD",
                        event.getVenue()
                );

                // In-app notification
                notificationRepository.save(Notification.builder()
                        .recipient(student)
                        .title(title)
                        .message(message)
                        .type(NotificationType.EVENT)
                        .eventId(event.getId())
                        .read(false)
                        .senderName("TECHNO-X Auto-Alert")
                        .build());

                // Email notification
                emailService.sendNotificationEmail(
                        student.getEmail(),
                        student.getName(),
                        "[TECHNO-X] Event Tomorrow — " + event.getTitle(),
                        title,
                        message
                );

                // WhatsApp notification
                if (student.getMobile() != null && !student.getMobile().isBlank()) {
                    whatsAppService.sendWhatsAppNotification(
                            student.getMobile(),
                            student.getName(),
                            event.getTitle(),
                            event.getEventDate().toString(),
                            event.getVenue(),
                            "Automated Reminder: Your registered event is tomorrow! Digital Pass is required at the entry gate."
                    );
                }
            }
        }
        log.info("[SCHEDULER] Reminder job complete for {} events", tomorrowEvents.size());
    }

    /**
     * Every day at 11:59 PM — mark events as ONGOING if they start today
     */
    @Scheduled(cron = "0 59 23 * * *")
    @Transactional
    public void markEventsAsOngoing() {
        LocalDate today = LocalDate.now();
        List<Event> todayEvents = eventRepository.findByEventDateAndStatusIn(
                today,
                List.of(EventStatus.PUBLISHED, EventStatus.APPROVED)
        );
        for (Event event : todayEvents) {
            event.setStatus(EventStatus.ONGOING);
            eventRepository.save(event);
            log.info("[SCHEDULER] Marked event '{}' as ONGOING", event.getTitle());
        }
    }

    /**
     * Every hour — notify students when a new event is published (within last 1 hour)
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void notifyNewlyPublishedEvents() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        // Find events published in last 1 hour
        List<Event> newEvents = eventRepository.findRecentlyPublished(oneHourAgo, EventStatus.PUBLISHED);

        if (newEvents.isEmpty()) return;

        List<User> allStudents = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().name().equals("STUDENT")))
                .toList();

        for (Event event : newEvents) {
            log.info("[SCHEDULER] Notifying {} students about new event: {}", allStudents.size(), event.getTitle());
            for (User student : allStudents) {
                notificationRepository.save(Notification.builder()
                        .recipient(student)
                        .title("New Event: " + event.getTitle())
                        .message(String.format("A new event '%s' has been published! Category: %s. Venue: %s. Register before %s.",
                                event.getTitle(),
                                event.getCategory() != null ? event.getCategory().getName() : "General",
                                event.getVenue(),
                                event.getRegistrationDeadline()))
                        .type(NotificationType.EVENT)
                        .eventId(event.getId())
                        .read(false)
                        .senderName("TECHNO-X Auto-Alert")
                        .build());
            }
        }
    }
}
