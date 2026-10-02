package com.technox.auth.dto;

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
public class StudentRegisterRequest {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Full name is required")
    private String name;

    private String fatherName;

    private String dob;

    @NotBlank(message = "Course is required")
    private String course;

    @NotBlank(message = "Admission year is required")
    private String year;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Mobile number is required")
    private String mobile;

    @NotBlank(message = "Password is required")
    private String password;

    private String verificationType;
    private String verificationCode;
}
