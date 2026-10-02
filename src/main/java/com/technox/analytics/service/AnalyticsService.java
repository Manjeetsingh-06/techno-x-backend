package com.technox.analytics.service;

import com.technox.analytics.dto.AnalyticsDto;
import com.technox.attendance.repository.AttendanceRepository;
import com.technox.category.entity.Category;
import com.technox.category.repository.CategoryRepository;
import com.technox.event.entity.Event;
import com.technox.event.entity.EventStatus;
import com.technox.event.repository.EventRepository;
import com.technox.registration.entity.Registration;
import com.technox.registration.repository.RegistrationRepository;
import com.technox.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EventRepository eventRepository;
    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;
    private final AttendanceRepository attendanceRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public AnalyticsDto getSystemAnalytics() {
        long totalEvents = eventRepository.count();
        long totalStudents = studentRepository.count();
        long totalRegistrations = registrationRepository.count();
        long activeEvents = eventRepository.countByStatus(EventStatus.PUBLISHED);
        long totalAttendance = attendanceRepository.count();

        double attendanceRate = totalRegistrations > 0
                ? Math.round(((double) totalAttendance / totalRegistrations) * 1000.0) / 10.0
                : 0.0;

        List<Event> events = eventRepository.findAll();
        List<Registration> registrations = registrationRepository.findAll();
        List<Category> categories = categoryRepository.findAll();

        // Registration trends by date
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd");
        Map<String, Long> trendMap = registrations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getRegisteredAt().format(dtf),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        List<Map<String, Object>> registrationTrends = new ArrayList<>();
        trendMap.forEach((date, count) -> {
            Map<String, Object> point = new HashMap<>();
            point.put("date", date);
            point.put("registrations", count);
            registrationTrends.add(point);
        });

        // Category distribution
        Map<Long, Long> eventCountByCategory = events.stream()
                .filter(e -> e.getCategory() != null)
                .collect(Collectors.groupingBy(e -> e.getCategory().getId(), Collectors.counting()));

        List<Map<String, Object>> categoryDistribution = categories.stream().map(cat -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", cat.getName());
            map.put("count", eventCountByCategory.getOrDefault(cat.getId(), 0L));
            map.put("color", cat.getColorCode());
            return map;
        }).toList();

        // Status distribution
        Map<EventStatus, Long> statusCount = events.stream()
                .collect(Collectors.groupingBy(Event::getStatus, Collectors.counting()));

        List<Map<String, Object>> statusDistribution = Arrays.stream(EventStatus.values()).map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", s.name());
            map.put("count", statusCount.getOrDefault(s, 0L));
            return map;
        }).toList();

        // Committee performance
        Map<String, Long> committeeEvents = events.stream()
                .filter(e -> e.getCommitteeCode() != null)
                .collect(Collectors.groupingBy(Event::getCommitteeCode, Collectors.counting()));

        List<Map<String, Object>> committeePerformance = committeeEvents.entrySet().stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("committee", e.getKey());
            map.put("eventsOrganized", e.getValue());
            return map;
        }).toList();

        return AnalyticsDto.builder()
                .totalEvents(totalEvents)
                .totalStudents(totalStudents)
                .totalRegistrations(totalRegistrations)
                .activeEvents(activeEvents)
                .overallAttendanceRate(attendanceRate)
                .registrationTrends(registrationTrends)
                .categoryDistribution(categoryDistribution)
                .statusDistribution(statusDistribution)
                .committeePerformance(committeePerformance)
                .build();
    }
}
