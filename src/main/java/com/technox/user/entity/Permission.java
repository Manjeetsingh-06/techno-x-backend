package com.technox.user.entity;

import com.technox.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private PermissionType name;

    @Column(name = "description", length = 255)
    private String description;
}
