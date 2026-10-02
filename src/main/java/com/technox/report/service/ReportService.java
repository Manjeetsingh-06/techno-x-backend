package com.technox.report.service;

import com.technox.attendance.entity.Attendance;
import com.technox.attendance.repository.AttendanceRepository;
import com.technox.event.entity.Event;
import com.technox.event.repository.EventRepository;
import com.technox.registration.entity.Registration;
import com.technox.registration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    public byte[] generateEventAttendanceCsv(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        List<Attendance> attendanceList = attendanceRepository.findByEventId(eventId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);

        writer.println("Event ID,Event Title,Student ID,Student Name,Course,Year,Status,Scanned At,Operator Name");
        for (Attendance a : attendanceList) {
            writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\"%n",
                    event.getId(),
                    event.getTitle().replace("\"", "\"\""),
                    a.getStudent().getStudentId(),
                    a.getStudent().getUser().getName().replace("\"", "\"\""),
                    a.getStudent().getCourse(),
                    a.getStudent().getYear(),
                    a.getStatus(),
                    a.getScannedAt(),
                    a.getOperatorName() != null ? a.getOperatorName().replace("\"", "\"\"") : ""
            );
        }
        writer.flush();
        return out.toByteArray();
    }

    @Transactional(readOnly = true)
    public byte[] generateEventRegistrationsCsv(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        List<Registration> regs = registrationRepository.findWithFilters(eventId, null, null, org.springframework.data.domain.Pageable.unpaged()).getContent();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);

        writer.println("Registration ID,Pass ID,Student ID,Student Name,Email,Course,Year,Status,Registered At");
        for (Registration r : regs) {
            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\"%n",
                    r.getRegistrationId(),
                    r.getDigitalPassId(),
                    r.getStudent().getStudentId(),
                    r.getStudent().getUser().getName().replace("\"", "\"\""),
                    r.getStudent().getUser().getEmail(),
                    r.getStudent().getCourse(),
                    r.getStudent().getYear(),
                    r.getStatus(),
                    r.getRegisteredAt()
            );
        }
        writer.flush();
        return out.toByteArray();
    }
}
