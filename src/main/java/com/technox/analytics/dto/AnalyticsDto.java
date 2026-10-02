package com.technox.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDto {
    private long totalEvents;
    private long totalStudents;
    private long totalRegistrations;
    private long activeEvents;
    private double overallAttendanceRate;
    private List<Map<String, Object>> registrationTrends;
    private List<Map<String, Object>> categoryDistribution;
    private List<Map<String, Object>> statusDistribution;
    private List<Map<String, Object>> committeePerformance;
}
