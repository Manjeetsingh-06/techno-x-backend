package com.technox.student.controller;

import com.technox.common.dto.ApiResponse;
import com.technox.common.dto.PagedResponse;
import com.technox.student.dto.CreateStudentRequest;
import com.technox.student.dto.StudentDto;
import com.technox.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Endpoints for managing student profiles and records")
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Get paginated list of students with optional search, course, and year filters")
    public ResponseEntity<ApiResponse<PagedResponse<StudentDto>>> getStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        PagedResponse<StudentDto> result = studentService.getStudents(search, course, year, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE', 'STUDENT')")
    @Operation(summary = "Get student profile by internal database ID")
    public ResponseEntity<ApiResponse<StudentDto>> getStudentById(@PathVariable Long id) {
        StudentDto result = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/code/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE', 'STUDENT')")
    @Operation(summary = "Get student profile by student roll / registration code (e.g. TGI2025BCA768)")
    public ResponseEntity<ApiResponse<StudentDto>> getStudentByStudentId(@PathVariable String studentId) {
        StudentDto result = studentService.getStudentByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'MANAGEMENT_COMMITTEE', 'STUDENT')")
    @Operation(summary = "Get student profile by linked User ID")
    public ResponseEntity<ApiResponse<StudentDto>> getStudentByUserId(@PathVariable Long userId) {
        StudentDto result = studentService.getStudentByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create student record and user account")
    public ResponseEntity<ApiResponse<StudentDto>> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        StudentDto result = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Student created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @Operation(summary = "Update student details and profile")
    public ResponseEntity<ApiResponse<StudentDto>> updateStudent(
            @PathVariable Long id,
            @RequestBody StudentDto updateDto
    ) {
        StudentDto result = studentService.updateStudentProfile(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success(result, "Student profile updated successfully"));
    }
}
