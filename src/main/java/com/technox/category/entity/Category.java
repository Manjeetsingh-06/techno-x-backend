package com.technox.category.entity;

import com.technox.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "event_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "color_code", length = 20)
    private String colorCode;
}
