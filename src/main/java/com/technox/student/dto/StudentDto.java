package com.technox.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String mobile;
    private String studentId;
    private String fatherName;
    private String dob;
    private String course;
    private String year;
    private String semester;
    private String department;
    private String bloodGroup;
    private String address;
    private String avatarUrl;
    private String qrCode;
    private LocalDateTime createdAt;
}
