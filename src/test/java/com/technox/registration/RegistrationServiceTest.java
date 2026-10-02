package com.technox.registration;

import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import com.technox.email.EmailService;
import com.technox.event.entity.Event;
import com.technox.event.entity.EventStatus;
import com.technox.event.repository.EventRepository;
import com.technox.registration.dto.RegistrationDto;
import com.technox.registration.entity.Registration;
import com.technox.registration.repository.RegistrationRepository;
import com.technox.registration.service.RegistrationService;
import com.technox.student.entity.Student;
import com.technox.student.repository.StudentRepository;
import com.technox.user.entity.User;
import com.technox.waitlist.entity.WaitlistEntry;
import com.technox.waitlist.repository.WaitlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;
    @Mock
    private WaitlistRepository waitlistRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private RegistrationService registrationService;

    private Event testEvent;
    private Student testStudent;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .name("Rohan Sharma")
                .email("student@technox.test")
                .build();
        testUser.setId(10L);

        testStudent = Student.builder()
                .user(testUser)
                .studentId("TGI2025BCA768")
                .course("BCA")
                .year("3rd Year")
                .build();
        testStudent.setId(1L);

        testEvent = Event.builder()
                .title("TechnoHacks 2026")
                .capacity(50)
                .registeredCount(10)
                .waitlistCount(0)
                .status(EventStatus.PUBLISHED)
                .eventDate(LocalDate.now().plusDays(10))
                .registrationDeadline(LocalDateTime.now().plusDays(5))
                .build();
        testEvent.setId(100L);
    }

    @Test
    @DisplayName("Should successfully register student when capacity is available")
    void testRegisterStudent_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(registrationRepository.existsByEventIdAndStudentId(100L, 1L)).thenReturn(false);
        when(eventRepository.findByIdWithLock(100L)).thenReturn(Optional.of(testEvent));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> {
            Registration r = i.getArgument(0);
            r.setId(500L);
            return r;
        });

        RegistrationDto result = registrationService.registerStudent(100L, 1L);

        assertNotNull(result);
        assertEquals(11, testEvent.getRegisteredCount());
        assertNotNull(result.getDigitalPassId());
        assertNotNull(result.getQrToken());
        verify(emailService).sendRegistrationConfirmation(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should reject registration if student is already registered")
    void testRegisterStudent_AlreadyRegistered() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(registrationRepository.existsByEventIdAndStudentId(100L, 1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                registrationService.registerStudent(100L, 1L));

        assertEquals(ErrorCode.CONFLICT, ex.getErrorCode());
        verify(eventRepository, never()).findByIdWithLock(any());
    }

    @Test
    @DisplayName("Should add student to waitlist when event is full")
    void testRegisterStudent_EventFull_AddsToWaitlist() {
        testEvent.setCapacity(50);
        testEvent.setRegisteredCount(50);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(registrationRepository.existsByEventIdAndStudentId(100L, 1L)).thenReturn(false);
        when(eventRepository.findByIdWithLock(100L)).thenReturn(Optional.of(testEvent));
        when(waitlistRepository.existsByEventIdAndStudentId(100L, 1L)).thenReturn(false);
        when(waitlistRepository.findMaxPositionByEventId(100L)).thenReturn(3);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                registrationService.registerStudent(100L, 1L));

        assertEquals(ErrorCode.EVENT_FULL, ex.getErrorCode());
        verify(waitlistRepository).save(any(WaitlistEntry.class));
        assertEquals(1, testEvent.getWaitlistCount());
    }
}
