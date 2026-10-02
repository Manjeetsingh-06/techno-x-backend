package com.technox.achievement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAchievementDto {
    private Long id;
    private Long studentId;
    private AchievementDto achievement;
    private LocalDateTime earnedAt;
    private String associatedEventTitle;
}
