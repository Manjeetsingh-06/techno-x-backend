package com.technox.student.dto;

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
public class CreateStudentRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String mobile;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    private String fatherName;
    private String dob;

    @NotBlank(message = "Course is required")
    private String course;

    @NotBlank(message = "Year is required")
    private String year;

    private String semester;
    private String department;
    private String bloodGroup;
    private String address;
    private String password;
}
