package com.technox.waitlist.service;

import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import com.technox.email.EmailService;
import com.technox.event.entity.Event;
import com.technox.event.repository.EventRepository;
import com.technox.registration.dto.RegistrationDto;
import com.technox.registration.entity.Registration;
import com.technox.registration.entity.RegistrationStatus;
import com.technox.registration.repository.RegistrationRepository;
import com.technox.registration.service.RegistrationService;
import com.technox.waitlist.dto.WaitlistEntryDto;
import com.technox.waitlist.entity.WaitlistEntry;
import com.technox.waitlist.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final RegistrationService registrationService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<WaitlistEntryDto> getWaitlistForEvent(Long eventId) {
        return waitlistRepository.findByEventIdAndStatusOrderByPositionAsc(eventId, "WAITLISTED")
                .stream().map(this::mapToDto).toList();
    }

    @Transactional
    public RegistrationDto promoteStudent(Long waitlistEntryId, Long coordinatorUserId) {
        WaitlistEntry entry = waitlistRepository.findById(waitlistEntryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Waitlist entry not found: " + waitlistEntryId));

        if (!"WAITLISTED".equalsIgnoreCase(entry.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Student is already " + entry.getStatus());
        }

        Event event = eventRepository.findByIdWithLock(entry.getEvent().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found"));

        entry.setStatus("PROMOTED");
        entry.setPromotedAt(LocalDateTime.now());
        entry.setPromotedByUserId(coordinatorUserId);
        waitlistRepository.save(entry);

        event.setRegisteredCount(event.getRegisteredCount() + 1);
        event.setWaitlistCount(Math.max(0, event.getWaitlistCount() - 1));
        eventRepository.save(event);

        String regId = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy")) + "-" + ThreadLocalRandom.current().nextInt(100000, 999999);
        String passId = "PASS-PRM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String qrToken = "TX-QR-" + UUID.randomUUID();

        Registration reg = Registration.builder()
                .registrationId(regId)
                .event(event)
                .student(entry.getStudent())
                .status(RegistrationStatus.REGISTERED)
                .registeredAt(LocalDateTime.now())
                .digitalPassId(passId)
                .qrToken(qrToken)
                .passValidity("VALID")
                .manualOverride(true)
                .overrideReason("Promoted by coordinator from waitlist")
                .overriddenByUserId(coordinatorUserId)
                .build();

        reg = registrationRepository.save(reg);

        emailService.sendRegistrationConfirmation(
                entry.getStudent().getUser().getEmail(),
                entry.getStudent().getUser().getName(),
                event.getTitle(),
                regId,
                passId
        );

        log.info("Coordinator {} promoted waitlisted student {} for event {}", coordinatorUserId, entry.getStudent().getStudentId(), event.getTitle());
        return registrationService.mapToDto(reg);
    }

    @Transactional
    public void cancelWaitlistEntry(Long waitlistEntryId) {
        WaitlistEntry entry = waitlistRepository.findById(waitlistEntryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Waitlist entry not found: " + waitlistEntryId));

        entry.setStatus("CANCELLED");
        waitlistRepository.save(entry);

        Event event = eventRepository.findById(entry.getEvent().getId()).orElseThrow();
        event.setWaitlistCount(Math.max(0, event.getWaitlistCount() - 1));
        eventRepository.save(event);
    }

    public WaitlistEntryDto mapToDto(WaitlistEntry entry) {
        return WaitlistEntryDto.builder()
                .id(entry.getId())
                .eventId(entry.getEvent().getId())
                .eventTitle(entry.getEvent().getTitle())
                .studentId(entry.getStudent().getId())
                .studentName(entry.getStudent().getUser().getName())
                .studentCode(entry.getStudent().getStudentId())
                .studentEmail(entry.getStudent().getUser().getEmail())
                .position(entry.getPosition())
                .status(entry.getStatus())
                .createdAt(entry.getCreatedAt())
                .promotedAt(entry.getPromotedAt())
                .build();
    }
}
