package com.technox.achievement.entity;

import com.technox.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement extends BaseEntity {

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code; // e.g. FIRST_EVENT, HACKATHON_CHAMPION

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "badge_icon", length = 50)
    private String badgeIcon;

    @Column(name = "color_theme", length = 50)
    private String colorTheme;

    @Column(name = "criteria", length = 255)
    private String criteria;
}
