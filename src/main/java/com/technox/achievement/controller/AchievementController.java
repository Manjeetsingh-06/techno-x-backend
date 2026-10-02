package com.technox.achievement.controller;

import com.technox.achievement.dto.AchievementDto;
import com.technox.achievement.dto.StudentAchievementDto;
import com.technox.achievement.service.AchievementService;
import com.technox.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
@Tag(name = "Achievements", description = "Endpoints for student badges, participation trophies, and honors")
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    @Operation(summary = "Get list of all system badges and achievements")
    public ResponseEntity<ApiResponse<List<AchievementDto>>> getAllAchievements() {
        return ResponseEntity.ok(ApiResponse.success(achievementService.getAllAchievements()));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get earned badges and achievements for a student")
    public ResponseEntity<ApiResponse<List<StudentAchievementDto>>> getStudentAchievements(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(achievementService.getStudentAchievements(studentId)));
    }
}
