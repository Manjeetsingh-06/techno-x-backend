package com.technox.attendance.service;

import com.technox.attendance.dto.AttendanceCorrectionDto;
import com.technox.attendance.dto.AttendanceCorrectionRequest;
import com.technox.attendance.dto.AttendanceDto;
import com.technox.attendance.dto.MarkAttendanceRequest;
import com.technox.attendance.entity.Attendance;
import com.technox.attendance.entity.AttendanceCorrection;
import com.technox.attendance.entity.AttendanceStatus;
import com.technox.attendance.repository.AttendanceCorrectionRepository;
import com.technox.attendance.repository.AttendanceRepository;
import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import com.technox.registration.entity.Registration;
import com.technox.registration.entity.RegistrationStatus;
import com.technox.registration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceCorrectionRepository correctionRepository;
    private final RegistrationRepository registrationRepository;

    @Transactional
    public AttendanceDto markAttendance(MarkAttendanceRequest request, Long operatorUserId, String operatorName, String operatorRole) {
        Registration registration = findRegistration(request);

        if (!registration.getEvent().getId().equals(request.getEventId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Registration pass belongs to a different event!");
        }

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Registration pass is cancelled and invalid.");
        }

        if (attendanceRepository.existsByEventIdAndStudentId(request.getEventId(), registration.getStudent().getId())) {
            Attendance existing = attendanceRepository.findByEventIdAndStudentId(request.getEventId(), registration.getStudent().getId()).orElseThrow();
            throw new BusinessException(ErrorCode.CONFLICT, "Attendance already marked as " + existing.getStatus() + " at " + existing.getScannedAt());
        }

        Attendance attendance = Attendance.builder()
                .event(registration.getEvent())
                .student(registration.getStudent())
                .registration(registration)
                .status(request.getStatus() != null ? request.getStatus() : AttendanceStatus.PRESENT)
                .scannedAt(LocalDateTime.now())
                .scannedByUserId(operatorUserId)
                .operatorName(operatorName)
                .operatorRole(operatorRole)
                .corrected(false)
                .build();

        attendance = attendanceRepository.save(attendance);

        // Update pass validity
        registration.setPassValidity("USED");
        registrationRepository.save(registration);

        log.info("Attendance marked for student {} at event {} by {}",
                registration.getStudent().getStudentId(), registration.getEvent().getTitle(), operatorName);

        return mapToDto(attendance);
    }

    @Transactional
    public AttendanceDto correctAttendance(Long attendanceId, AttendanceCorrectionRequest request, Long correctorUserId, String correctorName) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Attendance record not found: " + attendanceId));

        AttendanceStatus oldStatus = attendance.getStatus();

        AttendanceCorrection correction = AttendanceCorrection.builder()
                .attendance(attendance)
                .previousStatus(oldStatus)
                .newStatus(request.getNewStatus())
                .reason(request.getReason())
                .correctedByUserId(correctorUserId)
                .correctedByName(correctorName)
                .build();

        correctionRepository.save(correction);

        attendance.setStatus(request.getNewStatus());
        attendance.setCorrected(true);
        attendance = attendanceRepository.save(attendance);

        log.info("Attendance ID {} corrected from {} to {} by {}", attendanceId, oldStatus, request.getNewStatus(), correctorName);
        return mapToDto(attendance);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto> getEventAttendance(Long eventId) {
        return attendanceRepository.findByEventId(eventId).stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceCorrectionDto> getAttendanceCorrections(Long attendanceId) {
        return correctionRepository.findByAttendanceId(attendanceId).stream()
                .map(this::mapCorrectionToDto).toList();
    }

    private Registration findRegistration(MarkAttendanceRequest request) {
        if (request.getToken() != null && !request.getToken().isBlank()) {
            String token = request.getToken().trim();
            return registrationRepository.findByQrToken(token)
                    .or(() -> registrationRepository.findByRegistrationId(token))
                    .or(() -> registrationRepository.findByEventIdAndStudentId(request.getEventId(), Long.parseLong(token)))
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "No valid registration pass found for token: " + token));
        }

        if (request.getStudentId() != null) {
            return registrationRepository.findByEventIdAndStudentId(request.getEventId(), request.getStudentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Student is not registered for this event."));
        }

        throw new BusinessException(ErrorCode.BAD_REQUEST, "Either pass token or student ID must be provided.");
    }

    public AttendanceDto mapToDto(Attendance attendance) {
        return AttendanceDto.builder()
                .id(attendance.getId())
                .eventId(attendance.getEvent().getId())
                .eventTitle(attendance.getEvent().getTitle())
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getUser().getName())
                .studentCode(attendance.getStudent().getStudentId())
                .studentEmail(attendance.getStudent().getUser().getEmail())
                .course(attendance.getStudent().getCourse())
                .year(attendance.getStudent().getYear())
                .registrationId(attendance.getRegistration().getId())
                .digitalPassId(attendance.getRegistration().getDigitalPassId())
                .status(attendance.getStatus())
                .scannedAt(attendance.getScannedAt())
                .scannedByUserId(attendance.getScannedByUserId())
                .operatorName(attendance.getOperatorName())
                .operatorRole(attendance.getOperatorRole())
                .corrected(attendance.isCorrected())
                .build();
    }

    public AttendanceCorrectionDto mapCorrectionToDto(AttendanceCorrection c) {
        return AttendanceCorrectionDto.builder()
                .id(c.getId())
                .attendanceId(c.getAttendance().getId())
                .previousStatus(c.getPreviousStatus())
                .newStatus(c.getNewStatus())
                .reason(c.getReason())
                .correctedByUserId(c.getCorrectedByUserId())
                .correctedByName(c.getCorrectedByName())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
