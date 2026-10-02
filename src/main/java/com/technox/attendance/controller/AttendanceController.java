package com.technox.attendance.controller;

import com.technox.attendance.dto.AttendanceCorrectionDto;
import com.technox.attendance.dto.AttendanceCorrectionRequest;
import com.technox.attendance.dto.AttendanceDto;
import com.technox.attendance.dto.MarkAttendanceRequest;
import com.technox.attendance.service.AttendanceService;
import com.technox.common.dto.ApiResponse;
import com.technox.user.entity.User;
import com.technox.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Endpoints for event gate attendance scanning, QR check-in, and corrections")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Get complete attendance roster for an event")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> getEventAttendance(@PathVariable Long eventId) {
        List<AttendanceDto> roster = attendanceService.getEventAttendance(eventId);
        return ResponseEntity.ok(ApiResponse.success(roster));
    }

    @PostMapping("/mark")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Scan student QR code or digital pass to mark attendance")
    public ResponseEntity<ApiResponse<AttendanceDto>> markAttendance(
            @Valid @RequestBody MarkAttendanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User operator = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        String roleStr = userDetails.getAuthorities().stream().findFirst().map(Object::toString).orElse("OPERATOR");

        AttendanceDto dto = attendanceService.markAttendance(
                request,
                operator.getId(),
                operator.getName(),
                roleStr
        );
        return ResponseEntity.ok(ApiResponse.success(dto, "Attendance verified successfully"));
    }

    @PostMapping("/{id}/correct")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    @Operation(summary = "Correct attendance status with reason and audit log")
    public ResponseEntity<ApiResponse<AttendanceDto>> correctAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceCorrectionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User corrector = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        AttendanceDto dto = attendanceService.correctAttendance(
                id,
                request,
                corrector.getId(),
                corrector.getName()
        );
        return ResponseEntity.ok(ApiResponse.success(dto, "Attendance corrected successfully"));
    }

    @GetMapping("/{id}/corrections")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "View correction audit trail for an attendance entry")
    public ResponseEntity<ApiResponse<List<AttendanceCorrectionDto>>> getCorrections(@PathVariable Long id) {
        List<AttendanceCorrectionDto> list = attendanceService.getAttendanceCorrections(id);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
