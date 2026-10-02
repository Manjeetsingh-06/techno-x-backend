package com.technox.attendance;

import com.technox.attendance.dto.AttendanceCorrectionRequest;
import com.technox.attendance.dto.AttendanceDto;
import com.technox.attendance.dto.MarkAttendanceRequest;
import com.technox.attendance.entity.Attendance;
import com.technox.attendance.entity.AttendanceStatus;
import com.technox.attendance.repository.AttendanceCorrectionRepository;
import com.technox.attendance.repository.AttendanceRepository;
import com.technox.attendance.service.AttendanceService;
import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import com.technox.event.entity.Event;
import com.technox.registration.entity.Registration;
import com.technox.registration.entity.RegistrationStatus;
import com.technox.registration.repository.RegistrationRepository;
import com.technox.student.entity.Student;
import com.technox.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private AttendanceCorrectionRepository correctionRepository;
    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Event testEvent;
    private Student testStudent;
    private Registration testRegistration;

    @BeforeEach
    void setUp() {
        User user = User.builder().name("Rohan Sharma").email("student@technox.test").build();
        user.setId(5L);

        testStudent = Student.builder()
                .user(user)
                .studentId("TGI2025BCA768")
                .course("BCA")
                .year("3rd Year")
                .build();
        testStudent.setId(10L);

        testEvent = Event.builder()
                .title("TechnoHacks 2026")
                .build();
        testEvent.setId(100L);

        testRegistration = Registration.builder()
                .registrationId("TX-2026-112233")
                .digitalPassId("PASS-ABC-123")
                .qrToken("TX-QR-XYZ")
                .event(testEvent)
                .student(testStudent)
                .status(RegistrationStatus.REGISTERED)
                .passValidity("VALID")
                .build();
        testRegistration.setId(200L);
    }

    @Test
    @DisplayName("Should mark attendance successfully with valid QR token")
    void testMarkAttendance_Success() {
        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .eventId(100L)
                .token("TX-QR-XYZ")
                .status(AttendanceStatus.PRESENT)
                .build();

        when(registrationRepository.findByQrToken("TX-QR-XYZ")).thenReturn(Optional.of(testRegistration));
        when(attendanceRepository.existsByEventIdAndStudentId(100L, 10L)).thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> {
            Attendance a = i.getArgument(0);
            a.setId(999L);
            return a;
        });

        AttendanceDto result = attendanceService.markAttendance(
                request, 1L, "Dr. Malhotra", "FACULTY"
        );

        assertNotNull(result);
        assertEquals(AttendanceStatus.PRESENT, result.getStatus());
        assertEquals("USED", testRegistration.getPassValidity());
        verify(attendanceRepository).save(any(Attendance.class));
        verify(registrationRepository).save(testRegistration);
    }

    @Test
    @DisplayName("Should reject duplicate attendance scan")
    void testMarkAttendance_DuplicateScan() {
        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .eventId(100L)
                .token("TX-QR-XYZ")
                .build();

        Attendance existing = Attendance.builder()
                .event(testEvent)
                .student(testStudent)
                .registration(testRegistration)
                .status(AttendanceStatus.PRESENT)
                .scannedAt(LocalDateTime.now())
                .build();

        when(registrationRepository.findByQrToken("TX-QR-XYZ")).thenReturn(Optional.of(testRegistration));
        when(attendanceRepository.existsByEventIdAndStudentId(100L, 10L)).thenReturn(true);
        when(attendanceRepository.findByEventIdAndStudentId(100L, 10L)).thenReturn(Optional.of(existing));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                attendanceService.markAttendance(request, 1L, "Dr. Malhotra", "FACULTY"));

        assertEquals(ErrorCode.CONFLICT, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should log attendance correction")
    void testCorrectAttendance_Success() {
        Attendance attendance = Attendance.builder()
                .event(testEvent)
                .student(testStudent)
                .registration(testRegistration)
                .status(AttendanceStatus.PRESENT)
                .build();
        attendance.setId(888L);

        when(attendanceRepository.findById(888L)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        AttendanceCorrectionRequest req = new AttendanceCorrectionRequest(AttendanceStatus.ABSENT, "Student left venue early");
        AttendanceDto corrected = attendanceService.correctAttendance(888L, req, 1L, "Dr. Malhotra");

        assertNotNull(corrected);
        assertEquals(AttendanceStatus.ABSENT, corrected.getStatus());
        assertTrue(corrected.isCorrected());
        verify(correctionRepository).save(any());
    }
}
