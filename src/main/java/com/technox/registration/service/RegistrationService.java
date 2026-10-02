package com.technox.registration.service;

import com.technox.common.dto.PagedResponse;
import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import com.technox.email.EmailService;
import com.technox.event.entity.Event;
import com.technox.event.entity.EventStatus;
import com.technox.event.repository.EventRepository;
import com.technox.registration.dto.ManualRegistrationRequest;
import com.technox.registration.dto.RegistrationDto;
import com.technox.registration.entity.Registration;
import com.technox.registration.entity.RegistrationStatus;
import com.technox.registration.repository.RegistrationRepository;
import com.technox.student.entity.Student;
import com.technox.student.repository.StudentRepository;
import com.technox.waitlist.entity.WaitlistEntry;
import com.technox.waitlist.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final WaitlistRepository waitlistRepository;
    private final EventRepository eventRepository;
    private final StudentRepository studentRepository;
    private final EmailService emailService;

    @Transactional
    public RegistrationDto registerStudent(Long eventId, Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Student not found with ID: " + studentId));

        if (registrationRepository.existsByEventIdAndStudentId(eventId, studentId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "You are already registered for this event.");
        }

        // PESSIMISTIC_WRITE lock prevents over-allocation in high-concurrency registration rushes
        Event event = eventRepository.findByIdWithLock(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found with ID: " + eventId));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Event is not open for registrations (Status: " + event.getStatus() + ")");
        }

        if (event.getRegistrationDeadline() != null && LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Registration deadline for this event has passed.");
        }

        // Capacity check
        if (event.getRegisteredCount() >= event.getCapacity()) {
            if (waitlistRepository.existsByEventIdAndStudentId(eventId, studentId)) {
                throw new BusinessException(ErrorCode.CONFLICT, "You are already on the waitlist for this event.");
            }

            int position = waitlistRepository.findMaxPositionByEventId(eventId) + 1;
            WaitlistEntry waitlistEntry = WaitlistEntry.builder()
                    .event(event)
                    .student(student)
                    .position(position)
                    .status("WAITLISTED")
                    .build();
            waitlistRepository.save(waitlistEntry);

            event.setWaitlistCount(event.getWaitlistCount() + 1);
            eventRepository.save(event);

            log.info("Event {} full. Student {} added to waitlist at position {}", eventId, student.getStudentId(), position);
            throw new BusinessException(ErrorCode.EVENT_FULL, "Event capacity reached! You have been added to the waitlist at position #" + position);
        }

        // Slot available: increment capacity & issue registration
        event.setRegisteredCount(event.getRegisteredCount() + 1);
        eventRepository.save(event);

        String regId = generateRegistrationId();
        String passId = "PASS-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase();
        String qrToken = "TX-QR-" + UUID.randomUUID();

        Registration registration = Registration.builder()
                .registrationId(regId)
                .event(event)
                .student(student)
                .status(RegistrationStatus.REGISTERED)
                .registeredAt(LocalDateTime.now())
                .digitalPassId(passId)
                .qrToken(qrToken)
                .passValidity("VALID")
                .manualOverride(false)
                .build();

        registration = registrationRepository.save(registration);

        // Send email confirmation
        emailService.sendRegistrationConfirmation(
                student.getUser().getEmail(),
                student.getUser().getName(),
                event.getTitle(),
                regId,
                passId
        );

        log.info("Student {} registered successfully for event {}. Reg ID: {}", student.getStudentId(), event.getTitle(), regId);
        return mapToDto(registration);
    }

    @Transactional
    public RegistrationDto manualRegister(ManualRegistrationRequest request, Long coordinatorUserId) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Student not found with ID: " + request.getStudentId()));

        if (registrationRepository.existsByEventIdAndStudentId(request.getEventId(), request.getStudentId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Student is already registered for this event.");
        }

        Event event = eventRepository.findByIdWithLock(request.getEventId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found with ID: " + request.getEventId()));

        event.setRegisteredCount(event.getRegisteredCount() + 1);
        eventRepository.save(event);

        String regId = generateRegistrationId();
        String passId = "PASS-OVR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String qrToken = "TX-QR-" + UUID.randomUUID();

        Registration registration = Registration.builder()
                .registrationId(regId)
                .event(event)
                .student(student)
                .status(RegistrationStatus.REGISTERED)
                .registeredAt(LocalDateTime.now())
                .digitalPassId(passId)
                .qrToken(qrToken)
                .passValidity("VALID")
                .manualOverride(true)
                .overrideReason(request.getReason())
                .overriddenByUserId(coordinatorUserId)
                .build();

        registration = registrationRepository.save(registration);
        log.info("Coordinator {} manually registered student {} for event {}", coordinatorUserId, student.getStudentId(), event.getId());
        return mapToDto(registration);
    }

    @Transactional
    public void cancelRegistration(Long registrationId, Long requestingUserId, boolean isAdminOrCoordinator) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Registration not found with ID: " + registrationId));

        if (!isAdminOrCoordinator && !registration.getStudent().getUser().getId().equals(requestingUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "You are not authorized to cancel this registration.");
        }

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            return;
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setPassValidity("VOID");
        registrationRepository.save(registration);

        // Lock event to release slot and auto-promote from waitlist
        Event event = eventRepository.findByIdWithLock(registration.getEvent().getId()).orElseThrow();
        event.setRegisteredCount(Math.max(0, event.getRegisteredCount() - 1));

        // Auto-promote top waitlist entry if any
        List<WaitlistEntry> waitlist = waitlistRepository.findByEventIdAndStatusOrderByPositionAsc(event.getId(), "WAITLISTED");
        if (!waitlist.isEmpty()) {
            WaitlistEntry topEntry = waitlist.get(0);
            topEntry.setStatus("PROMOTED");
            topEntry.setPromotedAt(LocalDateTime.now());
            waitlistRepository.save(topEntry);

            event.setRegisteredCount(event.getRegisteredCount() + 1);
            event.setWaitlistCount(Math.max(0, event.getWaitlistCount() - 1));

            // Create promoted registration
            String regId = generateRegistrationId();
            String passId = "PASS-WL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String qrToken = "TX-QR-" + UUID.randomUUID();

            Registration promotedReg = Registration.builder()
                    .registrationId(regId)
                    .event(event)
                    .student(topEntry.getStudent())
                    .status(RegistrationStatus.REGISTERED)
                    .registeredAt(LocalDateTime.now())
                    .digitalPassId(passId)
                    .qrToken(qrToken)
                    .passValidity("VALID")
                    .manualOverride(false)
                    .build();

            registrationRepository.save(promotedReg);
            emailService.sendRegistrationConfirmation(
                    topEntry.getStudent().getUser().getEmail(),
                    topEntry.getStudent().getUser().getName(),
                    event.getTitle(),
                    regId,
                    passId
            );
            log.info("Auto-promoted student {} from waitlist for event {}", topEntry.getStudent().getStudentId(), event.getTitle());
        }

        eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RegistrationDto> getAllRegistrations(Long eventId, RegistrationStatus status, String search, Pageable pageable) {
        Page<Registration> page = registrationRepository.findWithFilters(eventId, status, search, pageable);
        List<RegistrationDto> dtos = page.getContent().stream().map(this::mapToDto).toList();
        return PagedResponse.of(dtos, page);
    }

    @Transactional(readOnly = true)
    public List<RegistrationDto> getStudentRegistrations(Long studentId) {
        return registrationRepository.findByStudentId(studentId).stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public RegistrationDto getRegistrationById(Long id) {
        Registration reg = registrationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Registration not found with ID: " + id));
        return mapToDto(reg);
    }

    @Transactional(readOnly = true)
    public RegistrationDto getRegistrationByPassId(String passId) {
        Registration reg = registrationRepository.findByRegistrationId(passId)
                .or(() -> registrationRepository.findByQrToken(passId))
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "No registration found for token/pass: " + passId));
        return mapToDto(reg);
    }

    public RegistrationDto mapToDto(Registration reg) {
        return RegistrationDto.builder()
                .id(reg.getId())
                .registrationId(reg.getRegistrationId())
                .eventId(reg.getEvent().getId())
                .eventTitle(reg.getEvent().getTitle())
                .eventDate(reg.getEvent().getEventDate())
                .eventVenue(reg.getEvent().getVenue())
                .eventCategory(reg.getEvent().getCategory() != null ? reg.getEvent().getCategory().getName() : null)
                .studentId(reg.getStudent().getId())
                .studentName(reg.getStudent().getUser().getName())
                .studentCode(reg.getStudent().getStudentId())
                .studentEmail(reg.getStudent().getUser().getEmail())
                .course(reg.getStudent().getCourse())
                .year(reg.getStudent().getYear())
                .status(reg.getStatus())
                .registeredAt(reg.getRegisteredAt())
                .digitalPassId(reg.getDigitalPassId())
                .qrToken(reg.getQrToken())
                .passValidity(reg.getPassValidity())
                .manualOverride(reg.isManualOverride())
                .overrideReason(reg.getOverrideReason())
                .overriddenByUserId(reg.getOverriddenByUserId())
                .build();
    }

    private String generateRegistrationId() {
        int randomSeq = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy")) + "-" + randomSeq;
    }
}
