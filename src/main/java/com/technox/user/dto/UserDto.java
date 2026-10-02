package com.technox.user.dto;

import com.technox.user.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String uuid;
    private String name;
    private String email;
    private String mobile;
    private UserStatus status;
    private boolean enabled;
    private boolean emailVerified;
    private boolean mobileVerified;
    private LocalDateTime lastLoginAt;
    private Set<String> roles;
    private Set<String> permissions;

    // Optional profile fields
    private String studentId;
    private String course;
    private String year;
    private String semester;
    private String department;
    private String designation;
    private String committeeCode;
    private String committeeName;
    private String roleTitle;
    private String avatarUrl;
    private String qrCode;
}
