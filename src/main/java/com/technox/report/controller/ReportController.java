package com.technox.report.controller;

import com.technox.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints for downloading CSV event rosters and attendance sheets")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/event/{eventId}/attendance/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Download event attendance roster as CSV")
    public ResponseEntity<byte[]> downloadAttendanceCsv(@PathVariable Long eventId) {
        byte[] csvData = reportService.generateEventAttendanceCsv(eventId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"event-" + eventId + "-attendance.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @GetMapping("/event/{eventId}/registrations/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Download event registrations as CSV")
    public ResponseEntity<byte[]> downloadRegistrationsCsv(@PathVariable Long eventId) {
        byte[] csvData = reportService.generateEventRegistrationsCsv(eventId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"event-" + eventId + "-registrations.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
