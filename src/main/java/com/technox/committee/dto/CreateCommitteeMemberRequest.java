package com.technox.committee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommitteeMemberRequest {

    @NotBlank(message = "Member name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String mobile;

    @NotBlank(message = "Committee code is required")
    private String committeeCode;

    private String committeeName;

    private String roleTitle;

    private String department;

    private String password;
}
