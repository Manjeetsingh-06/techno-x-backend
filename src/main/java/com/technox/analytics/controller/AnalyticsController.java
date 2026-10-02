package com.technox.analytics.controller;

import com.technox.analytics.dto.AnalyticsDto;
import com.technox.analytics.service.AnalyticsService;
import com.technox.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for platform metrics, attendance percentages, and registration trends")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Get overall college event analytics and chart metrics")
    public ResponseEntity<ApiResponse<AnalyticsDto>> getAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getSystemAnalytics()));
    }
}
