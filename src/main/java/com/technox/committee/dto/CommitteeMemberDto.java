package com.technox.committee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitteeMemberDto {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String mobile;
    private String committeeCode;
    private String committeeName;
    private String roleTitle;
    private String department;
    private LocalDateTime createdAt;
}
