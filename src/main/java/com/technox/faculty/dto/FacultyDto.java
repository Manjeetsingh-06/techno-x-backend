package com.technox.faculty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyDto {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String mobile;
    private String facultyCode;
    private String designation;
    private String department;
    private String specialization;
    private String officeRoom;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
