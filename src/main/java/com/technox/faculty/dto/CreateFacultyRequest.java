package com.technox.faculty.dto;

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
public class CreateFacultyRequest {

    @NotBlank(message = "Faculty name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String mobile;

    private String facultyCode;

    private String designation;

    @NotBlank(message = "Department is required")
    private String department;

    private String specialization;

    private String officeRoom;

    private String password;
}
