package com.technox.achievement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementDto {
    private Long id;
    private String code;
    private String title;
    private String description;
    private String badgeIcon;
    private String colorTheme;
    private String criteria;
}
