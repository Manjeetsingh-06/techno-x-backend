package com.technox.event.controller;

import com.technox.common.dto.ApiResponse;
import com.technox.common.dto.PagedResponse;
import com.technox.event.dto.CreateEventRequest;
import com.technox.event.dto.EventApprovalRequest;
import com.technox.event.dto.EventDto;
import com.technox.event.dto.UpdateEventRequest;
import com.technox.event.entity.EventStatus;
import com.technox.event.service.EventService;
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

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Endpoints for discovering, planning, approving, and managing college events")
public class EventController {

    private final EventService eventService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get paginated events with optional filters (status, category, committee, search)")
    public ResponseEntity<ApiResponse<PagedResponse<EventDto>>> getEvents(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String committeeCode,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "eventDate,asc") String sort
    ) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        PagedResponse<EventDto> result = eventService.getEvents(status, categoryId, committeeCode, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event details by ID")
    public ResponseEntity<ApiResponse<EventDto>> getEventById(@PathVariable Long id) {
        EventDto result = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Get event details by UUID")
    public ResponseEntity<ApiResponse<EventDto>> getEventByUuid(@PathVariable String uuid) {
        EventDto result = eventService.getEventByUuid(uuid);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Create and schedule a new event proposal")
    public ResponseEntity<ApiResponse<EventDto>> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        EventDto result = eventService.createEvent(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Event created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Update event details")
    public ResponseEntity<ApiResponse<EventDto>> updateEvent(
            @PathVariable Long id,
            @RequestBody UpdateEventRequest request
    ) {
        EventDto result = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Event updated successfully"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    @Operation(summary = "Approve or reject a submitted event proposal")
    public ResponseEntity<ApiResponse<EventDto>> approveEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventApprovalRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        EventDto result = eventService.approveEvent(id, user.getId(), request.getApproved(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success(result, request.getApproved() ? "Event approved" : "Event rejected"));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Publish an approved event to students")
    public ResponseEntity<ApiResponse<EventDto>> publishEvent(@PathVariable Long id) {
        EventDto result = eventService.publishEvent(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Event published to campus live feed"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Cancel an event with stated reason")
    public ResponseEntity<ApiResponse<EventDto>> cancelEvent(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Cancelled by coordinator") String reason
    ) {
        EventDto result = eventService.cancelEvent(id, reason);
        return ResponseEntity.ok(ApiResponse.success(result, "Event cancelled"));
    }
}
