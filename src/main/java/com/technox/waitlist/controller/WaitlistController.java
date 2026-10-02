package com.technox.waitlist.controller;

import com.technox.common.dto.ApiResponse;
import com.technox.registration.dto.RegistrationDto;
import com.technox.user.entity.User;
import com.technox.user.repository.UserRepository;
import com.technox.waitlist.dto.WaitlistEntryDto;
import com.technox.waitlist.service.WaitlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
@Tag(name = "Waitlist", description = "Endpoints for managing waitlisted students and manual queue promotions")
public class WaitlistController {

    private final WaitlistService waitlistService;
    private final UserRepository userRepository;

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Get list of waitlisted students for an event ordered by position")
    public ResponseEntity<ApiResponse<List<WaitlistEntryDto>>> getWaitlistForEvent(@PathVariable Long eventId) {
        List<WaitlistEntryDto> list = waitlistService.getWaitlistForEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/{id}/promote")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Promote a waitlisted student to registered status")
    public ResponseEntity<ApiResponse<RegistrationDto>> promoteStudent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        RegistrationDto dto = waitlistService.promoteStudent(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(dto, "Student promoted from waitlist"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove waitlist entry")
    public ResponseEntity<ApiResponse<Void>> cancelWaitlist(@PathVariable Long id) {
        waitlistService.cancelWaitlistEntry(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Waitlist entry removed"));
    }
}
