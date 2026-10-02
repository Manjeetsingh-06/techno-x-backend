package com.technox.registration.controller;

import com.technox.common.dto.ApiResponse;
import com.technox.common.dto.PagedResponse;
import com.technox.registration.dto.CreateRegistrationRequest;
import com.technox.registration.dto.ManualRegistrationRequest;
import com.technox.registration.dto.RegistrationDto;
import com.technox.registration.entity.RegistrationStatus;
import com.technox.registration.service.RegistrationService;
import com.technox.student.entity.Student;
import com.technox.student.repository.StudentRepository;
import com.technox.user.entity.User;
import com.technox.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
@Tag(name = "Registrations", description = "Endpoints for student event registrations, digital passes, and capacity management")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Get paginated list of all registrations with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<RegistrationDto>>> getAllRegistrations(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) RegistrationStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "registeredAt"));
        PagedResponse<RegistrationDto> result = registrationService.getAllRegistrations(eventId, status, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get registration by ID")
    public ResponseEntity<ApiResponse<RegistrationDto>> getRegistrationById(@PathVariable Long id) {
        RegistrationDto dto = registrationService.getRegistrationById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get current student's registrations via JWT auth")
    public ResponseEntity<ApiResponse<List<RegistrationDto>>> getMyRegistrations(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Student profile not found"));
        List<RegistrationDto> list = registrationService.getStudentRegistrations(student.getId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Get registrations for a student (by DB ID — admin/faculty use)")
    public ResponseEntity<ApiResponse<List<RegistrationDto>>> getStudentRegistrations(@PathVariable Long studentId) {
        List<RegistrationDto> list = registrationService.getStudentRegistrations(studentId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/pass/{passId}")
    @Operation(summary = "Lookup registration by pass ID or QR code token")
    public ResponseEntity<ApiResponse<RegistrationDto>> getByPassId(@PathVariable String passId) {
        RegistrationDto dto = registrationService.getRegistrationByPassId(passId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Student registers for an event (capacity checked with concurrency lock)")
    public ResponseEntity<ApiResponse<RegistrationDto>> register(
            @Valid @RequestBody CreateRegistrationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Student profile not found for user: " + user.getEmail()));

        RegistrationDto dto = registrationService.registerStudent(request.getEventId(), student.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto, "Successfully registered for event"));
    }

    @PostMapping("/manual")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Coordinator manually registers a student with override reason")
    public ResponseEntity<ApiResponse<RegistrationDto>> manualRegister(
            @Valid @RequestBody ManualRegistrationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        RegistrationDto dto = registrationService.manualRegister(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto, "Student manually registered"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel an event registration (frees slot and auto-promotes waitlist)")
    public ResponseEntity<ApiResponse<Void>> cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        boolean isAdminOrCoordinator = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                               a.getAuthority().equals("ROLE_FACULTY") ||
                               a.getAuthority().equals("ROLE_MANAGEMENT_COMMITTEE"));

        registrationService.cancelRegistration(id, user.getId(), isAdminOrCoordinator);
        return ResponseEntity.ok(ApiResponse.success(null, "Registration cancelled successfully"));
    }
}
