package com.technox.club.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubDto {
    private Long id;
    private String code;
    private String name;
    private String tagline;
    private String description;
    private String logoUrl;
    private String facultyCoordinator;
    private boolean active;
}
