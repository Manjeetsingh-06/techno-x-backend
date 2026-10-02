package com.technox.club.entity;

import com.technox.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "clubs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Club extends BaseEntity {

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "tagline", length = 150)
    private String tagline;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "faculty_coordinator", length = 100)
    private String facultyCoordinator;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
